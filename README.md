# Matching Engine
Exchange simulator which support live/replay market data

```Java
OrderBook orderBook = new OrderBook("0001.HK");
orderBook.addMessageListener(new MessageListener() {
    @Override
    public void onMessage(AbstractMessage message) {
        System.out.println("Received " + message.getClass().getSimpleName());
    }
});
Depth snapshot = new Depth(
        // Asks
        Arrays.asList(
                new PriceQuantity(100.5, 3000),
                new PriceQuantity(100.4, 2500),
                new PriceQuantity(100.3, 2000),
                new PriceQuantity(100.2, 1000),
                new PriceQuantity(100.1, 500)
        ),
        // Bids
        Arrays.asList(
                new PriceQuantity(99.9, 100),
                new PriceQuantity(99.8, 300),
                new PriceQuantity(99.7, 300),
                new PriceQuantity(99.6, 400),
                new PriceQuantity(99.5, 500)
        )
);
orderBook.onSnapshotUpdate(snapshot);
NewOrderSingle nos = (new NewOrderSingle.Builder())
        .from("TEST_CLIENT")
        .clOrdID("TEST-1")
        .symbol("0001.HK")
        .orderSide(OrderSide.BUY)
        .orderType(OrderType.LIMIT)
        .price(99.9)
        .quantity(500)
        .build();
orderBook.onNewOrderSingle(nos);
```

## Getting start
Java 8 is required in order to compile the project.
JUnit 5 is required if you would like to run some unit tests.

## How it works
```
 NOS/Cancel  +--------------+   Snapshot  +--------------+
------------>|  Order Book  |<------------|  Market Data |
<------------|              |----+        +--------------+
     ER      +--------------+    | Match
                       ^---------+
```

- Matching will be in price/time priority.
- Either a new order single or an update in market data will trigger a matching process in order book.
- Market liquidity (as indicated by market data) will be wrapped by a normal order and added to asks/bids queue accordingly. So that it simplifies the matching process.
- When there is an update in market data, Order Book will work out the change in market liquidity. If there is increased in liquidity, they will be added as new order to the queue. Otherwise, existing order representing market liquidity will be filled with that amount. If the liquidity have already taken by other orders then there will be no fill again.

## Todos
- Consider lot size
- Support replace request
- Support market order
- Support other session (Continuous session is supported currently)
- Publish market data (Trade & Quote)
- Support auto Done-for-day

## License
MIT