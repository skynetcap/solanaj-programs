# solanaj-programs - OpenBook v2
A SolanaJ module for interfacing with the OpenBook v2 DEX.
```xml
<dependency>
    <groupId>com.mmorrell</groupId>
    <artifactId>openbook</artifactId>
    <version>1.20.5</version>
</dependency>
```
## Code Examples

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

### Get Order Book
```java
// 2pMETA
Optional<OpenBookEventHeap> eventHeap = openBookManager.getEventHeap(PublicKey.valueOf("GY5HKym4yKNUpdHpBBiqLB3DHbrNKhLHDFTSLPK8AbFX"));
eventHeap.get().getOutEvents().forEach(openBookOutEvent -> {
    log.info("Out Event: {}", openBookOutEvent.toString());
});
```