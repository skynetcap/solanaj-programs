# Pyth in SolanaJ
A SolanaJ module for interfacing with Pyth.

## Maven Installation
```xml
<dependency>
        <groupId>com.mmorrell</groupId>
        <artifactId>solanaj-programs</artifactId>
        <version>1.9</version>
</dependency>
```

## Code Examples

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