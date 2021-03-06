package com.maukaming.aes.message;

public class CancelRejected extends AbstractMessage {
    private String clOrdID;
    private String origClOrdID;
    private String text;

    public CancelRejected() {
        super();
    }

    private CancelRejected(Builder builder) {
        super(builder);
        this.clOrdID = builder.clOrdID;
        this.origClOrdID = builder.origClOrdID;
        this.text = builder.text;
    }

    public String getClOrdID() {
        return clOrdID;
    }

    public String getOrigClOrdID() {
        return origClOrdID;
    }

    public String getText() {
        return text;
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private String clOrdID;
        private String origClOrdID;
        private String text;

        public Builder clOrdID(String clOrdID) {
            this.clOrdID = clOrdID;
            return this;
        }

        public Builder origClOrdID(String origClOrdID) {
            this.origClOrdID = origClOrdID;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public CancelRejected build() {
            return new CancelRejected(this);
        }
    }
}
