# Serum in SolanaJ
A SolanaJ module for interfacing with the Serum DEX.
## Code Examples

### Get Orderbook
```java
final PublicKey solUsdcPublicKey = new PublicKey("7xMDbYTCqQEcK2aM9LbetGtNFJpzKdfXzLL5juaLh4GJ");
final Market solUsdcMarket = new MarketBuilder()
        .setClient(new RpcClient())
        .setPublicKey(solUsdcPublicKey)
        .setRetrieveOrderBooks(true)
        .build();

final OrderBook bids = solUsdcMarket.getBidOrderBook();
```