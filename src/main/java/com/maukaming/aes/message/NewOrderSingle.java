package com.maukaming.aes.message;

import com.maukaming.aes.order.OrderSide;
import com.maukaming.aes.order.OrderType;

public class NewOrderSingle extends AbstractMessage {
    private String clOrdID;
    private String symbol;
    private OrderType orderType;
    private OrderSide orderSide;
    private double price;
    private long quantity;

    public NewOrderSingle(){
        super();
    }

    private NewOrderSingle(Builder builder) {
        super(builder);
        this.clOrdID = builder.clOrdID;
        this.symbol = builder.symbol;
        this.orderType = builder.orderType;
        this.orderSide = builder.orderSide;
        this.price = builder.price;
        this.quantity = builder.quantity;
    }

    public String getClOrdID() {
        return clOrdID;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public OrderSide getOrderSide() {
        return orderSide;
    }

    public double getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private String clOrdID;
        private String symbol;
        private OrderType orderType;
        private OrderSide orderSide;
        private double price;
        private long quantity;

        public Builder clOrdID(String clOrdID) {
            this.clOrdID = clOrdID;
            return this;
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder orderType(OrderType orderType) {
            this.orderType = orderType;
            return this;
        }

        public Builder orderSide(OrderSide orderSide) {
            this.orderSide = orderSide;
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

        public NewOrderSingle build() {
            return new NewOrderSingle(this);
        }
    }
}
