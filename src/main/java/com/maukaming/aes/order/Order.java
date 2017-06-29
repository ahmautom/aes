package com.maukaming.aes.order;

public class Order {
    private String from;
    private String clOrdID;
    private OrderStatus status = OrderStatus.NEW;
    private OrderSide side;
    private OrderType type;
    private String symbol;
    private double price;
    private long quantity;
    private double avgPrice;
    private long cumQuantity;

    public Order() {
    }

    private Order(Builder builder) {
        this.from = builder.senderID;
        this.clOrdID = builder.clOrdID;
        this.side = builder.side;
        this.type = builder.type;
        this.symbol = builder.symbol;
        this.price = builder.price;
        this.quantity = builder.quantity;
    }

    public void applyFill(double price, long quantity) {
        avgPrice = (avgPrice * cumQuantity + price * quantity) / (cumQuantity + quantity);
        cumQuantity += quantity;

        if (cumQuantity >= this.quantity) {
            status = OrderStatus.FILLED;
        } else {
            status = OrderStatus.PARTIALLY_FILLED;
        }
    }

    public void setRejected() {
        status = OrderStatus.REJECTED;
    }

    public void setCanceled() {
        status = OrderStatus.CANCELED;
    }

    public String getFrom() {
        return from;
    }

    public String getClOrdID() {
        return clOrdID;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public long getCumQuantity() {
        return cumQuantity;
    }

    public long getLeaveQuantity() {
        return quantity - cumQuantity;
    }

    public boolean isActive() {
        return status == OrderStatus.NEW || status == OrderStatus.PARTIALLY_FILLED;
    }

    public static class Builder {
        private String senderID;
        private String clOrdID;
        private OrderSide side;
        private OrderType type;
        private String symbol;
        private double price;
        private long quantity;

        public Builder from(String senderID) {
            this.senderID = senderID;
            return this;
        }

        public Builder clOrdID(String clOrdID) {
            this.clOrdID = clOrdID;
            return this;
        }

        public Builder side(OrderSide side) {
            this.side = side;
            return this;
        }

        public Builder type(OrderType type) {
            this.type = type;
            return this;
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder price(double price) {
            this.price = price;
            return this;
        }

        public Builder quantity(long quantity) {
            this.quantity = quantity;
            return this;
        }


        public Order build() {
            return new Order(this);
        }
    }
}
