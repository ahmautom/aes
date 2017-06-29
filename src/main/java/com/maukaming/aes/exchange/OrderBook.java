package com.maukaming.aes.exchange;

import com.maukaming.aes.message.*;
import com.maukaming.aes.order.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 *  Exchange simulator which support live/playback market data
 */
public class OrderBook {
    private final Map<String, Order> orders = new ConcurrentHashMap<>(); // ClOrdID -> Order
    private final Map<Double, List<Order>> asks = new ConcurrentSkipListMap<>(); // Price (Ascending) -> Order[] (Ordered by time of insert)
    private final Map<Double, List<Order>> bids = new ConcurrentSkipListMap<>(Comparator.reverseOrder()); // Price (Descending) -> Order[] (Ordered by time of insert)
    private final Set<MessageListener> messageListeners = ConcurrentHashMap.newKeySet();

    private final String symbol;
    private volatile Depth snapshot;

    public OrderBook(String symbol) {
        this.symbol = symbol;
    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public synchronized void onSnapshotUpdate(Depth snapshot) {
        // Work out the change in market data snapshot, then adjust the liquidity
        updateMarketLiquidity(true, snapshot);
        updateMarketLiquidity(false, snapshot);
        this.snapshot = snapshot;
        match(true);
        match(false);
    }

    private void updateMarketLiquidity(boolean isBuy, Depth snapshot) {
        Map<Double, List<Order>> limitOrders = isBuy ? bids : asks;
        Map<Double, Long> oldSnapshot = this.snapshot == null ? Collections.EMPTY_MAP : (isBuy ? this.snapshot.getBids() : this.snapshot.getAsks());
        Map<Double, Long> newSnapshot = isBuy ? snapshot.getBids() : snapshot.getAsks();

        // Remove all market liquidity order if no longer exists in new snapshot
        oldSnapshot.entrySet().stream()
            .filter(entry -> !newSnapshot.containsKey(entry.getKey()))
            .forEach(entry -> {
                List<Order> queue = limitOrders.get(entry.getKey());
                queue.removeIf(order -> order.getType() == OrderType.MARKET_LIQUIDITY);

                if (queue.isEmpty()) {
                    limitOrders.remove(entry.getKey());
                }
            });

        // Adjust existing market liquidity order
        newSnapshot.entrySet().stream()
            .forEach(entry -> {
                double price = entry.getKey();
                long delta =  entry.getValue() - oldSnapshot.getOrDefault(price, 0L);
                List<Order> queue = limitOrders.computeIfAbsent(price, p -> new LinkedList<>());

                // If there is increase in market liquidity, add it to the order queue
                if (delta > 0) {
                    queue.add((new Order.Builder())
                        .type(OrderType.MARKET_LIQUIDITY)
                        .price(price)
                        .quantity(delta)
                        .build());
                }
                // If there is decrease in market liquidity, fill/remove the corresponding market liquidity order, ignore the bit we already used up
                else {
                    long totalFillQty = 0;
                    for (Iterator<Order> it = queue.iterator(); totalFillQty < Math.abs(delta) && it.hasNext();) {
                        Order order = it.next();
                        if (order.getType() == OrderType.MARKET_LIQUIDITY) {
                            long fillQty = Math.min(order.getLeaveQuantity(), Math.abs(delta) - totalFillQty);
                            totalFillQty += fillQty;
                            order.applyFill(price, fillQty);
                            if (!order.isActive()) {
                                it.remove();
                            }
                        }
                    }
                }
            });
    }

    public synchronized Order onNewOrderSingle(NewOrderSingle message) {
        Order order = (new Order.Builder())
                .from(message.getFrom())
                .clOrdID(message.getClOrdID())
                .symbol(message.getSymbol())
                .side(message.getOrderSide())
                .type(message.getOrderType())
                .price(message.getPrice())
                .quantity(message.getQuantity())
                .build();
        Order duplicatedOrder = orders.putIfAbsent(order.getClOrdID(), order);

        if (duplicatedOrder != null) {
            order.setRejected();
            sendMessage((new OrderRejected.Builder())
                .to(message.getFrom())
                .clOrdID(message.getClOrdID())
                .text("Order already existed")
                .build());
            return order;
        }

        sendMessage((new OrderAccepted.Builder())
            .to(message.getFrom())
            .clOrdID(message.getClOrdID())
            .build());

        match(order);

        // Add the residue to the queue
        if (order.getStatus() != OrderStatus.FILLED) {
            Map<Double, List<Order>> limitOrders = order.getSide() == OrderSide.BUY ? bids : asks;
            List<Order> queue = limitOrders.computeIfAbsent(order.getPrice(), price -> new LinkedList<>());
            queue.add(order);
        }

        return order;
    }

    public synchronized Order onCancelRequest(CancelRequest message) {
        if (!orders.containsKey(message.getOrigClOrdID())) {
            sendMessage((new CancelRejected.Builder())
                .to(message.getFrom())
                .clOrdID(message.getClOrdID())
                .origClOrdID(message.getOrigClOrdID())
                .text("Cannot find order")
                .build()
            );
            return null;
        }

        Order order = orders.get(message.getOrigClOrdID());

        if (!order.isActive()) {
            sendMessage((new CancelRejected.Builder())
                .to(message.getFrom())
                .clOrdID(message.getClOrdID())
                .origClOrdID(message.getOrigClOrdID())
                .text("Too late to cancel")
                .build()
            );
            return order;
        }

        order.setCanceled();
        Map<Double, List<Order>> limitOrders = order.getSide() == OrderSide.BUY ? bids : asks;
        List<Order> queue = limitOrders.get(order.getPrice());
        queue.remove(order);

        if (queue.isEmpty()) {
            limitOrders.remove(order.getPrice());
        }

        sendMessage((new OrderCanceled.Builder())
                .to(message.getFrom())
                .clOrdID(message.getClOrdID())
                .origClOrdID(message.getOrigClOrdID())
                .build()
        );

        return order;
    }

    /**
     * Used whenever there is update in market liquidity, assuming there is no crossing between orders. The cross only
     * happens between market liquidity and orders.
     */
    private void match(boolean isBuy) {
        Map<Double, List<Order>> sideOrders = isBuy ? bids : asks;

        for (Iterator<Map.Entry<Double, List<Order>>> it = sideOrders.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Double, List<Order>> entry = it.next();
            List<Order> queue = entry.getValue();

            for (Iterator<Order> it2 = queue.iterator(); it2.hasNext();) {
                Order order = it2.next();

                if (order.getType() != OrderType.MARKET_LIQUIDITY) {
                    match(order);

                    if (order.getStatus() == OrderStatus.FILLED) {
                        it2.remove();
                    }
                }
            }

            if (queue.isEmpty()) {
                it.remove();
            }
        }
    }

    /**
     * Match an order with market liquidity or order in the book
     * @param order
     */
    private void match(Order order) {
        Map<Double, List<Order>> oppoOrders = order.getSide() == OrderSide.BUY ? asks : bids;

        for (Iterator<Map.Entry<Double, List<Order>>> it = oppoOrders.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Double, List<Order>> entry = it.next();
            double price = entry.getKey();

            if (order.getSide() == OrderSide.BUY) {
                if (order.getPrice() < price) {
                    break;
                }
            } else {
                if (order.getPrice() > price) {
                    break;
                }
            }

            List<Order> queue = entry.getValue();

            for (Iterator<Order> it2 = queue.iterator(); it2.hasNext();) {
                Order oppoOrder = it2.next();
                long fillQuantity = Math.min(order.getLeaveQuantity(), oppoOrder.getLeaveQuantity());
                order.applyFill(price, fillQuantity);
                oppoOrder.applyFill(price, fillQuantity);

                if (order.getType() != OrderType.MARKET_LIQUIDITY) {
                    sendMessage((new Fill.Builder())
                            .to(order.getFrom())
                            .clOrdID(order.getClOrdID())
                            .avgPrice(order.getAvgPrice())
                            .cumQuantity(order.getCumQuantity())
                            .leaveQuantity(order.getLeaveQuantity())
                            .lastPrice(price)
                            .lastShare(fillQuantity)
                            .build());
                }

                if (oppoOrder.getType() != OrderType.MARKET_LIQUIDITY) {
                    sendMessage((new Fill.Builder())
                            .to(oppoOrder.getFrom())
                            .clOrdID(oppoOrder.getClOrdID())
                            .avgPrice(oppoOrder.getAvgPrice())
                            .cumQuantity(oppoOrder.getCumQuantity())
                            .leaveQuantity(oppoOrder.getLeaveQuantity())
                            .lastPrice(price)
                            .lastShare(fillQuantity)
                            .build());
                }

                if (oppoOrder.getStatus() == OrderStatus.FILLED) {
                    it2.remove();
                }

                if (order.getStatus() == OrderStatus.FILLED) {
                    break;
                }
            }

            if (queue.isEmpty()) {
                it.remove();
            }

            if (order.getStatus() == OrderStatus.FILLED) {
                break;
            }
        }
    }

    public Depth generateDepth() {
        return new Depth(
                asks.entrySet().stream()
                        .map(entry -> new PriceQuantity(entry.getKey(), entry.getValue().stream()
                                .mapToLong(Order::getLeaveQuantity)
                                .sum()))
                        .collect(Collectors.toList()),
                bids.entrySet().stream()
                        .map(entry -> new PriceQuantity(entry.getKey(), entry.getValue().stream()
                                .mapToLong(Order::getLeaveQuantity)
                                .sum()))
                        .collect(Collectors.toList())
        );
    }

    private void sendMessage(AbstractMessage message) {
        messageListeners.forEach(messageListener -> messageListener.onMessage(message));
    }
}
