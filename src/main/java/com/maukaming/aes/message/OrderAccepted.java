package com.maukaming.aes.message;

public class OrderAccepted extends AbstractMessage {
    private String clOrdID;

    public OrderAccepted() {
        super();
    }

    private OrderAccepted(Builder builder) {
        super(builder);
        this.clOrdID = builder.clOrdID;
    }

    public String getClOrdID() {
        return clOrdID;
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private String clOrdID;

        public Builder clOrdID(String clOrdID) {
            this.clOrdID = clOrdID;
            return this;
        }

        public OrderAccepted build() {
            return new OrderAccepted(this);
        }
    }
}
