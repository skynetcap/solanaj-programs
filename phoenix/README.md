# SolanaJ-programs - Phoenix
A SolanaJ module for interfacing with the Ellipsis Phoenix DEX.
## Code Examples
```xml
<dependency>
    <groupId>com.mmorrell</groupId>
    <artifactId>openbook</artifactId>
    <version>1.20.6</version>
</dependency>
```

### Get All Markets and Orderbooks
```java
RpcClient client = new RpcClient(Cluster.MAINNET);
PhoenixManager phoenixManager = new PhoenixManager(client);
MetaplexManager metaplexManager = new MetaplexManager(client);

phoenixManager.getPhoenixMarkets().forEach(market -> {
    log.info("Market: {}", market.getMarketId().toBase58());
    
    metaplexManager.getTokenMetadata(market.getPhoenixMarketHeader().getBaseMintKey())
        .ifPresent(metadata -> log.info("Base token: {}", metadata.getSymbol()));
    metaplexManager.getTokenMetadata(market.getPhoenixMarketHeader().getQuoteMintKey())
        .ifPresent(metadata -> log.info("Quote token: {}", metadata.getSymbol()));
    
    market.getBidListNormalized().forEach(phoenixOrder -> {
        log.info(String.format("Bid: %.10f x %.4f, Trader: %s", phoenixOrder.getPrice(),
        phoenixOrder.getSize(), phoenixOrder.getTrader().toBase58()));
    });
    market.getAskListNormalized().forEach(phoenixOrder -> {
        log.info(String.format("Ask: %.10f x %.4f, Trader: %s", phoenixOrder.getPrice(),
        phoenixOrder.getSize(), phoenixOrder.getTrader().toBase58()));
    });
});
```

### Order Placement
```java
LimitOrderPacketRecord limitOrderPacketRecord = LimitOrderPacketRecord.builder()
    .clientOrderId(new byte[]{})
    .matchLimit(0)
    .numBaseLots(18L)
    .priceInTicks((long) (market.getBestBid().get().getFirst().getPriceInTicks() * .9995))
    .selfTradeBehavior((byte) 1)
    .side((byte) 0)
    .useOnlyDepositedFunds(false)
    .build();

tx.addInstruction(
    PhoenixProgram.placeLimitOrder(
        SOL_USDC_MARKET,
        tradingAccount.getPublicKey(),
        baseWallet,
        quoteWallet,
        market.getPhoenixMarketHeader().getBaseVaultKey(),
        market.getPhoenixMarketHeader().getQuoteVaultKey(),
        limitOrderPacketRecord
    )
);

// submit TX
```

### Manually deserialize a Market
```java
final AccountInfo marketAccountInfo = client.getApi().getAccountInfo(
                SOL_USDC_MARKET,
                Map.of("commitment", Commitment.PROCESSED)
        );

byte[] data = marketAccountInfo.getDecodedData();
PhoenixMarket market = PhoenixMarket.readPhoenixMarket(data);
```

### Get a single Market
```java
PhoenixManager phoenixManager = new PhoenixManager(client);

Optional<PhoenixMarket> marketOptional = phoenixManager.getMarket(SOL_USDC_MARKET, false);
marketOptional.ifPresent(market -> {
    log.info("Market: {}", market);
});
```