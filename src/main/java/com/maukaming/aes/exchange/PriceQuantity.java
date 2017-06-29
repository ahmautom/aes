package com.maukaming.aes.exchange;

public class PriceQuantity {
    private final double price;
    private final long quantity;

    public PriceQuantity(double price, long quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }
}
