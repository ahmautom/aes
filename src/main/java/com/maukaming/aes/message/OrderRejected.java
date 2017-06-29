package com.maukaming.aes.message;

public class OrderRejected extends AbstractMessage {
    private String clOrdID;
    private String text;

    public OrderRejected(){
        super();
    }

    private OrderRejected(Builder builder) {
        super(builder);
        this.clOrdID = builder.clOrdID;
        this.text = builder.text;
    }

    public String getClOrdID() {
        return clOrdID;
    }

    public String getText() {
        return text;
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private String clOrdID;
        private String text;

        public Builder clOrdID(String clOrdID) {
            this.clOrdID = clOrdID;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public OrderRejected build() {
            return new OrderRejected(this);
        }
    }
}
