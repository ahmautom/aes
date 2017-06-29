package com.maukaming.aes.exchange;

import java.util.*;

public class Depth {
    private final Map<Double, Long> asks;
    private final Map<Double, Long> bids;

    public Depth(List<PriceQuantity> asks, List<PriceQuantity> bids) {
        this.asks = Collections.unmodifiableMap(asks.stream().collect(HashMap::new, (map, pq) -> map.put(pq.getPrice(), pq.getQuantity()), HashMap::putAll));
        this.bids = Collections.unmodifiableMap(bids.stream().collect(HashMap::new, (map, pq) -> map.put(pq.getPrice(), pq.getQuantity()), HashMap::putAll));
    }

    public Depth(Map<Double, Long> asks, Map<Double, Long> bids) {
        this.asks = Collections.unmodifiableMap(asks);
        this.bids = Collections.unmodifiableMap(bids);
    }

    public Map<Double, Long> getAsks() {
        return asks;
    }

    public Map<Double, Long> getBids() {
        return bids;
    }
}
