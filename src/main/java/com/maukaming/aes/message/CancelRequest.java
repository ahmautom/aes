package com.maukaming.aes.message;

public class CancelRequest extends AbstractMessage {
    private String clOrdID;
    private String origClOrdID;

    public CancelRequest() {
        super();
    }

    private CancelRequest(Builder builder) {
        super(builder);
        this.clOrdID = builder.clOrdID;
        this.origClOrdID = builder.origClOrdID;
    }

    public String getClOrdID() {
        return clOrdID;
    }

    public String getOrigClOrdID() {
        return origClOrdID;
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private String clOrdID;
        private String origClOrdID;

        public Builder clOrdID(String clOrdID) {
            this.clOrdID = clOrdID;
            return this;
        }

        public Builder origClOrdID(String origClOrdID) {
            this.origClOrdID = origClOrdID;
            return this;
        }

        public CancelRequest build() {
            return new CancelRequest(this);
        }
    }
}
