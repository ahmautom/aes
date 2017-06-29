package com.maukaming.aes.message;

public class Fill extends AbstractMessage {
    private String clOrdID;
    private double avgPrice;
    private long cumQuantity;
    private long leaveQuantity;
    private double lastPrice;
    private long lastShare;

    public Fill() {
        super();
    }

    private Fill(Builder builder) {
        super(builder);
        this.clOrdID = builder.clOrdID;
        this.avgPrice = builder.avgPrice;
        this.cumQuantity = builder.cumQuantity;
        this.leaveQuantity = builder.leaveQuantity;
        this.lastPrice = builder.lastPrice;
        this.lastShare = builder.lastShare;
    }

    public String getClOrdID() {
        return clOrdID;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public long getCumQuantity() {
        return cumQuantity;
    }

    public long getLeaveQuantity() {
        return leaveQuantity;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public long getLastShare() {
        return lastShare;
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private String clOrdID;
        private double avgPrice;
        private long cumQuantity;
        private long leaveQuantity;
        private double lastPrice;
        private long lastShare;

        public Builder clOrdID(String clOrdID) {
            this.clOrdID = clOrdID;
            return this;
        }

        public Builder avgPrice(double avgPrice) {
            this.avgPrice = avgPrice;
            return this;
        }

        public Builder cumQuantity(long cumQuantity) {
            this.cumQuantity = cumQuantity;
            return this;
        }

        public Builder leaveQuantity(long leaveQuantity) {
            this.leaveQuantity = leaveQuantity;
            return this;
        }

        public Builder lastPrice(double lastPrice) {
            this.lastPrice = lastPrice;
            return this;
        }

        public Builder lastShare(long lastShare) {
            this.lastShare = lastShare;
            return this;
        }

        public Fill build() {
            return new Fill(this);
        }
    }
}