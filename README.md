# solanaj-programs

SolanaJ implementations of popular Solana programs including OpenBook v1 and v2, Phoenix, Pyth, Metaplex, and 
Bonfida.

# Installation
1. Add Maven dependency:

```xml
<dependency>
        <groupId>com.mmorrell</groupId>
        <artifactId>solanaj-programs</artifactId>
        <version>1.33.0</version>
</dependency>
```
Or just one dependency:
```xml
<dependency>
    <groupId>com.mmorrell</groupId>
    <artifactId>phoenix</artifactId>
    <version>1.33.0</version>
</dependency>
```

# Examples
## Bonfida

1. Pubkey to Twitter/X username
```java
@Test
public void getTwitterHandleTest() {
    String twitterHandle = namingManager.getTwitterHandle(skynetMainnetPubkey);

    LOGGER.info(twitterHandle);
    assertTrue(twitterHandle.equalsIgnoreCase("skynetcap"));
}
```

2. SOL domain to Pubkey
```java
@Test
public void getPublicKeyBySolDomainTest() {
    PublicKey publicKey = namingManager.getPublicKeyBySolDomain("skynet");
    LOGGER.info(String.format("skynet.sol = %s", publicKey.toBase58()));
}
```

3. Pubkey to SOL domain
```java
@Test
public void resolveTest() {
    LOGGER.info("Looking up domain for: " + skynetMainnetPubkey.toBase58());
    Optional<String> domainName = namingManager.getDomainNameByPubkey(skynetMainnetPubkey);

    // Verify we got a domain
    assertTrue(domainName.isPresent());

    String fullDomain = domainName.get() + ".sol";
    LOGGER.info("Domain = " + fullDomain);

    // Verify it matches skynet.sol
    assertEquals("skynet.sol", fullDomain);
}
```

## Metaplex
1. Get Token Metadata
```java
private static final PublicKey CRIPCO_TOKEN_MINT = new PublicKey("3uejHm24sWmniGA5m4j4S1DVuGqzYBR5DJpevND4mivq");

@Test
public void metadataTest() {
    final Optional<Metadata> optionalMetadata = metaplexManager.getTokenMetadata(CRIPCO_TOKEN_MINT);
    assertTrue(optionalMetadata.isPresent());

    final Metadata metadata = optionalMetadata.get();
    LOGGER.info(
            String.format(
                    "Metadata: %s",
                    metadata
            )
    );
}
```

## OpenBook v2
### Get SOL/USDC Orderbook
```java
// SOL/USDC
RpcClient client = new RpcClient("YOUR_RPC_HOST");
OpenBookManager openBookManager = new OpenBookManager(client);

OpenBookMarket solUsdc = openBookManager.getMarket(
        PublicKey.valueOf("C3YPL3kYCSYKsmHcHrPWx1632GUXGqi2yMXJbfeCc57q"),
        false,
        true
).get();

log.info("Bids: {}", solUsdc.getBidOrders());
log.info("Asks: {}", solUsdc.getAskOrders());

assertFalse(solUsdc.getBidOrders().isEmpty());
```
```text
[main] INFO OpenBookTest - Bids: [OpenBookOrder(price=67.6, size=0.007, trader=FsQhJepwFkqsghZ3fiCRNCP7qEQk3BVcwPiCJCza3MsB), OpenBookOrder(price=69.0, size=0.002, trader=9SMAjQPumpCPJNRakX1ofdgJ736Fz7TN6ywsurj789ZX), OpenBookOrder(price=65.0, size=0.046, trader=9SMAjQPumpCPJNRakX1ofdgJ736Fz7TN6ywsurj789ZX), OpenBookOrder(price=50.0, size=0.1, trader=9SMAjQPumpCPJNRakX1ofdgJ736Fz7TN6ywsurj789ZX), OpenBookOrder(price=2.0, size=0.181, trader=9SMAjQPumpCPJNRakX1ofdgJ736Fz7TN6ywsurj789ZX)]
[main] INFO OpenBookTest - Asks: [OpenBookOrder(price=80.0, size=0.1, trader=9SMAjQPumpCPJNRakX1ofdgJ736Fz7TN6ywsurj789ZX), OpenBookOrder(price=200.0, size=1.0, trader=4gpeoTcx9awU7cHWWZugRe1scHot5wZdCwyc5xLYQn5T), OpenBookOrder(price=86.0, size=0.02, trader=9SMAjQPumpCPJNRakX1ofdgJ736Fz7TN6ywsurj789ZX), OpenBookOrder(price=85.0, size=0.01, trader=9SMAjQPumpCPJNRakX1ofdgJ736Fz7TN6ywsurj789ZX)]
```

### Get All Markets
```java
RpcClient client = new RpcClient("YOUR_RPC_HOST");

OpenBookManager openBookManager = new OpenBookManager(client);
log.info("Market cache: {}", openBookManager.getOpenBookMarkets());
```

### Get Event Heap
```java
// 2pMETA
Optional<OpenBookEventHeap> eventHeap = openBookManager.getEventHeap(PublicKey.valueOf("GY5HKym4yKNUpdHpBBiqLB3DHbrNKhLHDFTSLPK8AbFX"));
eventHeap.get().getOutEvents().forEach(openBookOutEvent -> {
    log.info("Out Event: {}", openBookOutEvent.toString());
});
```

### Crank A Market
```java
PublicKey marketId = PublicKey.valueOf("5hYMkB5nAz9aJA33GizyPVH3VkqfkG7V4S2B5ykHxsiM");
Account tradingAccount = Account.fromJson(
        Resources.toString(Resources.getResource(PRIVATE_KEY_FILE), Charset.defaultCharset())
);
Optional<String> transactionId = openBookManager.consumeEvents(
        tradingAccount,
        marketId,
        8
);

if (transactionId.isPresent()) {
    log.info("Cranked events: {}", transactionId.get());
} else {
    log.info("No events found to consume.");
}
```

## Phoenix

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

## Pyth

### Iterate all products and get price
```java
private final RpcClient client = new RpcClient("https://rpc.ankr.com/solana");
private final PythManager pythManager = new PythManager(client);

final MappingAccount mappingAccount = pythManager.getMappingAccount(MAPPING_ACCOUNT);

final List<PublicKey> productAccountKeys = mappingAccount.getProductAccountKeys();
for (PublicKey productAccountKey : productAccountKeys) {
    final ProductAccount productAccount = pythManager.getProductAccount(productAccountKey);
    final PublicKey priceDataAccountKey = productAccount.getPriceAccountKey();

    final PriceDataAccount priceDataAccount = pythManager.getPriceDataAccount(priceDataAccountKey);

    LOGGER.info(
            String.format(
                    "Asset: %s, Price: %.2f",
                    productAccount.getProductAttributes().get("description"),
                    priceDataAccount.getAggregatePriceInfo().getPrice()
            )
    );
}
```