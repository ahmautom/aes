package com.maukaming.aes.exchange;

import com.maukaming.aes.TestClient;
import com.maukaming.aes.message.*;
import com.maukaming.aes.order.Order;
import com.maukaming.aes.order.OrderSide;
import com.maukaming.aes.order.OrderStatus;
import com.maukaming.aes.order.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderBookTest {
    private static final Depth SNAPSHOT1 = new Depth(
            Arrays.asList(
                    new PriceQuantity(100, 100),
                    new PriceQuantity(101, 200)
            ),
            Arrays.asList(
                    new PriceQuantity(99, 300),
                    new PriceQuantity(98, 400)
            )
    );
    private static final Depth SNAPSHOT2 = new Depth(
            Arrays.asList(
                    new PriceQuantity(99, 100),
                    new PriceQuantity(100, 200)
            ),
            Arrays.asList(
                    new PriceQuantity(98, 300),
                    new PriceQuantity(97, 400)
            )
    );
    private OrderBook orderBook;
    private TestClient client;

    @BeforeEach
    public void setUp() {
        orderBook = new OrderBook("0001.HK");
        client = new TestClient();
        orderBook.addMessageListener(client);
    }

    @Test
    public void testOnSnapshotUpdate() {
        orderBook.onSnapshotUpdate(SNAPSHOT1);

        Depth depth = orderBook.generateDepth();
        assertEquals(200L, (long) depth.getAsks().getOrDefault(101.0, 0L));
        assertEquals(0L, (long) depth.getBids().getOrDefault(101.0, 0L));
        assertEquals(100L, (long) depth.getAsks().getOrDefault(100.0, 0L));
        assertEquals(0L, (long) depth.getBids().getOrDefault(100.0, 0L));
        assertEquals(0L, (long) depth.getAsks().getOrDefault(99.0, 0L));
        assertEquals(300L, (long) depth.getBids().getOrDefault(99.0, 0L));
        assertEquals(0L, (long) depth.getAsks().getOrDefault(98.0, 0L));
        assertEquals(400L, (long) depth.getBids().getOrDefault(98.0, 0L));
    }

    @Test
    public void testOnSnapshotUpdateChange() {
        orderBook.onSnapshotUpdate(SNAPSHOT1);
        orderBook.onSnapshotUpdate(SNAPSHOT2);

        Depth depth = orderBook.generateDepth();
        assertEquals(200, (long) depth.getAsks().getOrDefault(100.0, 0L));
        assertEquals(0, (long) depth.getBids().getOrDefault(100.0, 0L));
        assertEquals(100, (long) depth.getAsks().getOrDefault(99.0, 0L));
        assertEquals(0, (long) depth.getBids().getOrDefault(99.0, 0L));
        assertEquals(0, (long) depth.getAsks().getOrDefault(98.0, 0L));
        assertEquals(300, (long) depth.getBids().getOrDefault(98.0, 0L));
        assertEquals(0, (long) depth.getAsks().getOrDefault(97.0, 0L));
        assertEquals(400, (long) depth.getBids().getOrDefault(97.0, 0L));
    }

    @Test
    public void testDuplicatedNewOrderSingleReject() {
        orderBook.onSnapshotUpdate(SNAPSHOT1);
        NewOrderSingle nos = (new NewOrderSingle.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-1")
                .symbol("0001.HK")
                .orderSide(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .price(90)
                .quantity(100)
                .build();
        orderBook.onNewOrderSingle(nos);
        Order order = orderBook.onNewOrderSingle(nos);

        assertEquals(OrderStatus.REJECTED, order.getStatus());
        List<AbstractMessage> messages = client.getMessages();
        assertEquals(2, messages.size());
        AbstractMessage msg = messages.get(0);

        assertTrue(msg instanceof OrderAccepted);
        OrderAccepted orderAccepted = (OrderAccepted) msg;
        assertEquals("TEST_CLIENT", orderAccepted.getTo());
        assertEquals("TEST-1", orderAccepted.getClOrdID());

        msg = messages.get(1);
        assertTrue(msg instanceof OrderRejected);
        OrderRejected orderRejected = (OrderRejected) msg;
        assertEquals("TEST_CLIENT", orderRejected.getTo());
        assertEquals("TEST-1", orderRejected.getClOrdID());
        assertEquals("Order already existed", orderRejected.getText());
    }

    @Test
    public void testOnNewOrderSinglePassive() {
        orderBook.onSnapshotUpdate(SNAPSHOT1);
        Order order = orderBook.onNewOrderSingle((new NewOrderSingle.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-1")
                .symbol("0001.HK")
                .orderSide(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .price(90)
                .quantity(100)
                .build());

        assertEquals("TEST_CLIENT", order.getFrom());
        assertEquals("TEST-1", order.getClOrdID());
        assertEquals("0001.HK", order.getSymbol());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(OrderType.LIMIT, order.getType());
        assertEquals(90, order.getPrice());
        assertEquals(100, order.getQuantity());
        assertEquals(0, order.getAvgPrice());
        assertEquals(0, order.getCumQuantity());
        assertEquals(100, order.getLeaveQuantity());
        assertTrue(order.isActive());
        assertEquals(OrderStatus.NEW, order.getStatus());

        List<AbstractMessage> messages = client.getMessages();
        assertEquals(1, messages.size());
        AbstractMessage msg = messages.get(0);
        assertTrue(msg instanceof OrderAccepted);
        OrderAccepted orderAccepted = (OrderAccepted) msg;
        assertEquals("TEST_CLIENT", orderAccepted.getTo());
        assertEquals("TEST-1", orderAccepted.getClOrdID());
    }

    @Test
    public void testOnNewOrderSinglePartiallyFilled() {
        orderBook.onSnapshotUpdate(SNAPSHOT1);
        Order order = orderBook.onNewOrderSingle((new NewOrderSingle.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-1")
                .symbol("0001.HK")
                .orderSide(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .price(100)
                .quantity(200)
                .build());

        assertEquals("TEST_CLIENT", order.getFrom());
        assertEquals("TEST-1", order.getClOrdID());
        assertEquals("0001.HK", order.getSymbol());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(OrderType.LIMIT, order.getType());
        assertEquals(100, order.getPrice());
        assertEquals(200, order.getQuantity());
        assertEquals(100, order.getAvgPrice());
        assertEquals(100, order.getCumQuantity());
        assertEquals(100, order.getLeaveQuantity());
        assertTrue(order.isActive());
        assertEquals(OrderStatus.PARTIALLY_FILLED, order.getStatus());

        List<AbstractMessage> messages = client.getMessages();
        assertEquals(2, messages.size());

        AbstractMessage msg = messages.get(0);
        assertTrue(msg instanceof OrderAccepted);
        OrderAccepted orderAccepted = (OrderAccepted) msg;
        assertEquals("TEST_CLIENT", orderAccepted.getTo());
        assertEquals("TEST-1", orderAccepted.getClOrdID());

        msg = messages.get(1);
        assertTrue(msg instanceof Fill);
        Fill fill = (Fill) msg;
        assertEquals("TEST_CLIENT", fill.getTo());
        assertEquals("TEST-1", fill.getClOrdID());
        assertEquals(100.0, fill.getAvgPrice());
        assertEquals(100, fill.getCumQuantity());
        assertEquals(100, fill.getLeaveQuantity());
        assertEquals(100.0, fill.getLastPrice());
        assertEquals(100, fill.getLastShare());
    }

    @Test
    public void testOnNewOrderSingleFilled() {
        orderBook.onSnapshotUpdate(SNAPSHOT1);
        Order order = orderBook.onNewOrderSingle((new NewOrderSingle.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-1")
                .symbol("0001.HK")
                .orderSide(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .price(100)
                .quantity(100)
                .build());

        assertEquals("TEST_CLIENT", order.getFrom());
        assertEquals("TEST-1", order.getClOrdID());
        assertEquals("0001.HK", order.getSymbol());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(OrderType.LIMIT, order.getType());
        assertEquals(100, order.getPrice());
        assertEquals(100, order.getQuantity());
        assertEquals(100, order.getAvgPrice());
        assertEquals(100, order.getCumQuantity());
        assertEquals(0, order.getLeaveQuantity());
        assertFalse(order.isActive());
        assertEquals(OrderStatus.FILLED, order.getStatus());

        List<AbstractMessage> messages = client.getMessages();
        assertEquals(2, messages.size());

        AbstractMessage msg = messages.get(0);
        assertTrue(msg instanceof OrderAccepted);
        OrderAccepted orderAccepted = (OrderAccepted) msg;
        assertEquals("TEST_CLIENT", orderAccepted.getTo());
        assertEquals("TEST-1", orderAccepted.getClOrdID());

        msg = messages.get(1);
        assertTrue(msg instanceof Fill);
        Fill fill = (Fill) msg;
        assertEquals("TEST_CLIENT", fill.getTo());
        assertEquals("TEST-1", fill.getClOrdID());
        assertEquals(100.0, fill.getAvgPrice());
        assertEquals(100, fill.getCumQuantity());
        assertEquals(0, fill.getLeaveQuantity());
        assertEquals(100.0, fill.getLastPrice());
        assertEquals(100, fill.getLastShare());
    }

    @Test
    public void testOnNewOrderSingleNoChangeInMarketLiquidityShouldNotFill() {
        orderBook.onSnapshotUpdate(SNAPSHOT1);
        Order order = orderBook.onNewOrderSingle((new NewOrderSingle.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-1")
                .symbol("0001.HK")
                .orderSide(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .price(100)
                .quantity(200)
                .build());
        orderBook.onSnapshotUpdate(SNAPSHOT1);

        assertEquals("TEST_CLIENT", order.getFrom());
        assertEquals("TEST-1", order.getClOrdID());
        assertEquals("0001.HK", order.getSymbol());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(OrderType.LIMIT, order.getType());
        assertEquals(100, order.getPrice());
        assertEquals(200, order.getQuantity());
        assertEquals(100, order.getAvgPrice());
        assertEquals(100, order.getCumQuantity());
        assertEquals(100, order.getLeaveQuantity());
        assertTrue(order.isActive());
        assertEquals(OrderStatus.PARTIALLY_FILLED, order.getStatus());

        List<AbstractMessage> messages = client.getMessages();
        assertEquals(2, messages.size());

        AbstractMessage msg = messages.get(0);
        assertTrue(msg instanceof OrderAccepted);
        OrderAccepted orderAccepted = (OrderAccepted) msg;
        assertEquals("TEST_CLIENT", orderAccepted.getTo());
        assertEquals("TEST-1", orderAccepted.getClOrdID());

        msg = messages.get(1);
        assertTrue(msg instanceof Fill);
        Fill fill = (Fill) msg;
        assertEquals("TEST_CLIENT", fill.getTo());
        assertEquals("TEST-1", fill.getClOrdID());
        assertEquals(100.0, fill.getAvgPrice());
        assertEquals(100, fill.getCumQuantity());
        assertEquals(100, fill.getLeaveQuantity());
        assertEquals(100.0, fill.getLastPrice());
        assertEquals(100, fill.getLastShare());
    }

    @Test
    public void testOnNewOrderSingleIncreaseInMarketLiquidityShouldFill() {
        orderBook.onSnapshotUpdate(SNAPSHOT1);
        Order order = orderBook.onNewOrderSingle((new NewOrderSingle.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-1")
                .symbol("0001.HK")
                .orderSide(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .price(100)
                .quantity(300)
                .build());
        orderBook.onSnapshotUpdate(SNAPSHOT2);

        assertEquals("TEST_CLIENT", order.getFrom());
        assertEquals("TEST-1", order.getClOrdID());
        assertEquals("0001.HK", order.getSymbol());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(OrderType.LIMIT, order.getType());
        assertEquals(100, order.getPrice());
        assertEquals(300, order.getQuantity());
        assertEquals(99.66, order.getAvgPrice(), 0.01);
        assertEquals(300, order.getCumQuantity());
        assertEquals(0, order.getLeaveQuantity());
        assertFalse(order.isActive());
        assertEquals(OrderStatus.FILLED, order.getStatus());

        order = orderBook.onNewOrderSingle((new NewOrderSingle.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-2")
                .symbol("0001.HK")
                .orderSide(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .price(100)
                .quantity(100)
                .build());

        assertEquals("TEST_CLIENT", order.getFrom());
        assertEquals("TEST-2", order.getClOrdID());
        assertEquals("0001.HK", order.getSymbol());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(OrderType.LIMIT, order.getType());
        assertEquals(100, order.getPrice());
        assertEquals(100, order.getQuantity());
        assertEquals(0, order.getAvgPrice());
        assertEquals(0, order.getCumQuantity());
        assertEquals(100, order.getLeaveQuantity());
        assertTrue(order.isActive());
        assertEquals(OrderStatus.NEW, order.getStatus());

        List<AbstractMessage> messages = client.getMessages();
        assertEquals(5, messages.size());

        AbstractMessage msg = messages.get(0);
        assertTrue(msg instanceof OrderAccepted);
        OrderAccepted orderAccepted = (OrderAccepted) msg;
        assertEquals("TEST_CLIENT", orderAccepted.getTo());
        assertEquals("TEST-1", orderAccepted.getClOrdID());

        msg = messages.get(1);
        assertTrue(msg instanceof Fill);
        Fill fill = (Fill) msg;
        assertEquals("TEST_CLIENT", fill.getTo());
        assertEquals("TEST-1", fill.getClOrdID());
        assertEquals(100.0, fill.getAvgPrice());
        assertEquals(100, fill.getCumQuantity());
        assertEquals(200, fill.getLeaveQuantity());
        assertEquals(100.0, fill.getLastPrice());
        assertEquals(100, fill.getLastShare());

        msg = messages.get(2);
        assertTrue(msg instanceof Fill);
        fill = (Fill) msg;
        assertEquals("TEST_CLIENT", fill.getTo());
        assertEquals("TEST-1", fill.getClOrdID());
        assertEquals(99.5, fill.getAvgPrice());
        assertEquals(200, fill.getCumQuantity());
        assertEquals(100, fill.getLeaveQuantity());
        assertEquals(99.0, fill.getLastPrice());
        assertEquals(100, fill.getLastShare());

        msg = messages.get(3);
        assertTrue(msg instanceof Fill);
        fill = (Fill) msg;
        assertEquals("TEST_CLIENT", fill.getTo());
        assertEquals("TEST-1", fill.getClOrdID());
        assertEquals(99.66, fill.getAvgPrice(), 0.01);
        assertEquals(300, fill.getCumQuantity());
        assertEquals(0, fill.getLeaveQuantity());
        assertEquals(100.0, fill.getLastPrice());
        assertEquals(100, fill.getLastShare());

        msg = messages.get(4);
        assertTrue(msg instanceof OrderAccepted);
        orderAccepted = (OrderAccepted) msg;
        assertEquals("TEST_CLIENT", orderAccepted.getTo());
        assertEquals("TEST-2", orderAccepted.getClOrdID());
    }

    @Test
    public void testChangeInQueuePositionShouldFill() {
        orderBook.onSnapshotUpdate(SNAPSHOT1);
        Order order = orderBook.onNewOrderSingle((new NewOrderSingle.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-1")
                .symbol("0001.HK")
                .orderSide(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .price(99)
                .quantity(100)
                .build());
        orderBook.onSnapshotUpdate(SNAPSHOT2);

        assertEquals("TEST_CLIENT", order.getFrom());
        assertEquals("TEST-1", order.getClOrdID());
        assertEquals("0001.HK", order.getSymbol());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(OrderType.LIMIT, order.getType());
        assertEquals(99, order.getPrice());
        assertEquals(100, order.getQuantity());
        assertEquals(99, order.getAvgPrice());
        assertEquals(100, order.getCumQuantity());
        assertEquals(0, order.getLeaveQuantity());
        assertFalse(order.isActive());
        assertEquals(OrderStatus.FILLED, order.getStatus());

        List<AbstractMessage> messages = client.getMessages();
        assertEquals(2, messages.size());

        AbstractMessage msg = messages.get(0);
        assertTrue(msg instanceof OrderAccepted);
        OrderAccepted orderAccepted = (OrderAccepted) msg;
        assertEquals("TEST_CLIENT", orderAccepted.getTo());
        assertEquals("TEST-1", orderAccepted.getClOrdID());

        msg = messages.get(1);
        assertTrue(msg instanceof Fill);
        Fill fill = (Fill) msg;
        assertEquals("TEST_CLIENT", fill.getTo());
        assertEquals("TEST-1", fill.getClOrdID());
        assertEquals(99, fill.getAvgPrice());
        assertEquals(100, fill.getCumQuantity());
        assertEquals(0, fill.getLeaveQuantity());
        assertEquals(99.0, fill.getLastPrice());
        assertEquals(100, fill.getLastShare());
    }

    @Test
    public void testOnNewOrderSingleIncreaseInMarketLiquidityShouldNotChangeQueuePosition() {
        orderBook.onSnapshotUpdate(SNAPSHOT1);
        Order order = orderBook.onNewOrderSingle((new NewOrderSingle.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-1")
                .symbol("0001.HK")
                .orderSide(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .price(97)
                .quantity(100)
                .build());
        orderBook.onSnapshotUpdate(SNAPSHOT2);
        orderBook.onNewOrderSingle((new NewOrderSingle.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-2")
                .symbol("0001.HK")
                .orderSide(OrderSide.SELL)
                .orderType(OrderType.LIMIT)
                .price(97)
                .quantity(400)
                .build());

        assertEquals("TEST_CLIENT", order.getFrom());
        assertEquals("TEST-1", order.getClOrdID());
        assertEquals("0001.HK", order.getSymbol());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(OrderType.LIMIT, order.getType());
        assertEquals(97, order.getPrice());
        assertEquals(100, order.getQuantity());
        assertEquals(97, order.getAvgPrice());
        assertEquals(100, order.getCumQuantity());
        assertEquals(0, order.getLeaveQuantity());
        assertFalse(order.isActive());
        assertEquals(OrderStatus.FILLED, order.getStatus());

        List<AbstractMessage> messages = client.getMessages();
        assertEquals(5, messages.size());

        AbstractMessage msg = messages.get(0);
        assertTrue(msg instanceof OrderAccepted);
        OrderAccepted orderAccepted = (OrderAccepted) msg;
        assertEquals("TEST_CLIENT", orderAccepted.getTo());
        assertEquals("TEST-1", orderAccepted.getClOrdID());

        msg = messages.get(1);
        assertTrue(msg instanceof OrderAccepted);
        orderAccepted = (OrderAccepted) msg;
        assertEquals("TEST_CLIENT", orderAccepted.getTo());
        assertEquals("TEST-2", orderAccepted.getClOrdID());

        msg = messages.get(2);
        assertTrue(msg instanceof Fill);
        Fill fill = (Fill) msg;
        assertEquals("TEST_CLIENT", fill.getTo());
        assertEquals("TEST-2", fill.getClOrdID());
        assertEquals(98.0, fill.getAvgPrice());
        assertEquals(300, fill.getCumQuantity());
        assertEquals(100, fill.getLeaveQuantity());
        assertEquals(98.0, fill.getLastPrice());
        assertEquals(300, fill.getLastShare());

        msg = messages.get(3);
        assertTrue(msg instanceof Fill);
        fill = (Fill) msg;
        assertEquals("TEST_CLIENT", fill.getTo());
        assertEquals("TEST-2", fill.getClOrdID());
        assertEquals(97.75, fill.getAvgPrice());
        assertEquals(400, fill.getCumQuantity());
        assertEquals(0, fill.getLeaveQuantity());
        assertEquals(97.0, fill.getLastPrice());
        assertEquals(100, fill.getLastShare());

        msg = messages.get(4);
        assertTrue(msg instanceof Fill);
        fill = (Fill) msg;
        assertEquals("TEST_CLIENT", fill.getTo());
        assertEquals("TEST-1", fill.getClOrdID());
        assertEquals(97.0, fill.getAvgPrice());
        assertEquals(100, fill.getCumQuantity());
        assertEquals(0, fill.getLeaveQuantity());
        assertEquals(97.0, fill.getLastPrice());
        assertEquals(100, fill.getLastShare());
    }

    @Test
    public void testOnCancelRequest() {
        orderBook.onSnapshotUpdate(SNAPSHOT1);
        Order order = orderBook.onNewOrderSingle((new NewOrderSingle.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-1")
                .symbol("0001.HK")
                .orderSide(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .price(99.0)
                .quantity(100)
                .build());

        orderBook.onCancelRequest((new CancelRequest.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-1.1")
                .origClOrdID("TEST-1")
                .build());

        Order order2 = orderBook.onNewOrderSingle((new NewOrderSingle.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-2")
                .symbol("0001.HK")
                .orderSide(OrderSide.SELL)
                .orderType(OrderType.LIMIT)
                .price(99.0)
                .quantity(400)
                .build());

        assertEquals("TEST_CLIENT", order.getFrom());
        assertEquals("TEST-1", order.getClOrdID());
        assertEquals("0001.HK", order.getSymbol());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(OrderType.LIMIT, order.getType());
        assertEquals(99, order.getPrice());
        assertEquals(100, order.getQuantity());
        assertEquals(0, order.getAvgPrice());
        assertEquals(0, order.getCumQuantity());
        assertEquals(100, order.getLeaveQuantity());
        assertFalse(order.isActive());
        assertEquals(OrderStatus.CANCELED, order.getStatus());

        assertEquals("TEST_CLIENT", order2.getFrom());
        assertEquals("TEST-2", order2.getClOrdID());
        assertEquals("0001.HK", order2.getSymbol());
        assertEquals(OrderSide.SELL, order2.getSide());
        assertEquals(OrderType.LIMIT, order2.getType());
        assertEquals(99, order2.getPrice());
        assertEquals(400, order2.getQuantity());
        assertEquals(99.0, order2.getAvgPrice());
        assertEquals(300, order2.getCumQuantity());
        assertEquals(100, order2.getLeaveQuantity());
        assertTrue(order2.isActive());
        assertEquals(OrderStatus.PARTIALLY_FILLED, order2.getStatus());

        List<AbstractMessage> messages = client.getMessages();
        assertEquals(4, messages.size());

        AbstractMessage msg = messages.get(0);
        assertTrue(msg instanceof OrderAccepted);
        OrderAccepted orderAccepted = (OrderAccepted) msg;
        assertEquals("TEST_CLIENT", orderAccepted.getTo());
        assertEquals("TEST-1", orderAccepted.getClOrdID());

        msg = messages.get(1);
        assertTrue(msg instanceof OrderCanceled);
        OrderCanceled orderCanceled = (OrderCanceled) msg;
        assertEquals("TEST_CLIENT", orderCanceled.getTo());
        assertEquals("TEST-1.1", orderCanceled.getClOrdID());
        assertEquals("TEST-1", orderCanceled.getOrigClOrdID());

        msg = messages.get(2);
        assertTrue(msg instanceof OrderAccepted);
        orderAccepted = (OrderAccepted) msg;
        assertEquals("TEST_CLIENT", orderAccepted.getTo());
        assertEquals("TEST-2", orderAccepted.getClOrdID());

        msg = messages.get(3);
        assertTrue(msg instanceof Fill);
        Fill fill = (Fill) msg;
        assertEquals("TEST_CLIENT", fill.getTo());
        assertEquals("TEST-2", fill.getClOrdID());
        assertEquals(99.0, fill.getAvgPrice());
        assertEquals(300, fill.getCumQuantity());
        assertEquals(100, fill.getLeaveQuantity());
        assertEquals(99.0, fill.getLastPrice());
        assertEquals(300, fill.getLastShare());
    }

    @Test
    public void testOnCancelRequestInvalidOrigOrdID() {
        orderBook.onSnapshotUpdate(SNAPSHOT1);
        Order order = orderBook.onCancelRequest((new CancelRequest.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-1.1")
                .origClOrdID("TEST-1")
                .build());

        assertNull(order);
        List<AbstractMessage> messages = client.getMessages();
        assertEquals(1, messages.size());

        AbstractMessage msg = messages.get(0);
        assertTrue(msg instanceof CancelRejected);
        CancelRejected cancelRejected = (CancelRejected) msg;
        assertEquals("TEST_CLIENT", cancelRejected.getTo());
        assertEquals("TEST-1.1", cancelRejected.getClOrdID());
        assertEquals("TEST-1", cancelRejected.getOrigClOrdID());
        assertEquals("Cannot find order", cancelRejected.getText());
    }

    @Test
    public void testOnCancelRequestTooLateToCancel() {
        orderBook.onSnapshotUpdate(SNAPSHOT1);
        Order order = orderBook.onNewOrderSingle((new NewOrderSingle.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-1")
                .symbol("0001.HK")
                .orderSide(OrderSide.BUY)
                .orderType(OrderType.LIMIT)
                .price(100.0)
                .quantity(100)
                .build());

        orderBook.onCancelRequest((new CancelRequest.Builder())
                .from("TEST_CLIENT")
                .clOrdID("TEST-1.1")
                .origClOrdID("TEST-1")
                .build());

        List<AbstractMessage> messages = client.getMessages();
        assertEquals(3, messages.size());

        AbstractMessage msg = messages.get(0);
        assertTrue(msg instanceof OrderAccepted);
        OrderAccepted orderAccepted = (OrderAccepted) msg;
        assertEquals("TEST_CLIENT", orderAccepted.getTo());
        assertEquals("TEST-1", orderAccepted.getClOrdID());

        msg = messages.get(1);
        assertTrue(msg instanceof Fill);
        Fill fill = (Fill) msg;
        assertEquals("TEST_CLIENT", fill.getTo());
        assertEquals("TEST-1", fill.getClOrdID());
        assertEquals(100.0, fill.getAvgPrice());
        assertEquals(100, fill.getCumQuantity());
        assertEquals(0, fill.getLeaveQuantity());
        assertEquals(100.0, fill.getLastPrice());
        assertEquals(100, fill.getLastShare());

        msg = messages.get(2);
        assertTrue(msg instanceof CancelRejected);
        CancelRejected cancelRejected = (CancelRejected) msg;
        assertEquals("TEST_CLIENT", cancelRejected.getTo());
        assertEquals("TEST-1.1", cancelRejected.getClOrdID());
        assertEquals("TEST-1", cancelRejected.getOrigClOrdID());
        assertEquals("Too late to cancel", cancelRejected.getText());
    }
}
