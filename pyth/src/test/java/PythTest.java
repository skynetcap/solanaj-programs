import com.mmorrell.pyth.listener.PriceDataAccountListener;
import com.mmorrell.pyth.manager.PythManager;
import com.mmorrell.pyth.model.MappingAccount;
import com.mmorrell.pyth.model.PriceDataAccount;
import com.mmorrell.pyth.model.ProductAccount;
import org.junit.jupiter.api.*;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.ws.SubscriptionWebSocketClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class PythTest {

    private final RpcClient client = new RpcClient(Cluster.MAINNET);
    private final SubscriptionWebSocketClient webSocketClient = SubscriptionWebSocketClient.getInstance(Cluster.MAINNET.getEndpoint());
    private final PythManager pythManager = new PythManager(client);
    private static final Logger LOGGER = Logger.getLogger(PythTest.class.getName());
    private static final int PYTH_MAGIC_NUMBER = (int) Long.parseLong("a1b2c3d4", 16);
    private static final int EXPECTED_PYTH_VERSION = 2;
    private static final int EXPECTED_EXPONENT = -8;
    private static final PublicKey MAPPING_ACCOUNT = PublicKey.valueOf("AHtgzX45WTKfkPG53L6WYhGEXwQkN1BVknET3sVsLL8J");

    @Test
    public void mappingAccount() {
        final MappingAccount mappingAccount = pythManager.getMappingAccount(MAPPING_ACCOUNT);

        LOGGER.info(
                String.format(
                        "Mapping Account = %s",
                        mappingAccount.toString()
                )
        );

        LOGGER.info(
                String.format(
                        "Mapping Account Pubkey = %s",
                        MAPPING_ACCOUNT.toBase58()
                )
        );

        // Magic Number
        int magicInt = mappingAccount.getMagicNumber();
        LOGGER.info(
                String.format(
                        "Magic number %d",
                        magicInt
                )
        );
        assertEquals(PYTH_MAGIC_NUMBER, magicInt);

        // Version
        int version = mappingAccount.getVersion();
        LOGGER.info(
                String.format(
                        "Version %d",
                        version
                )
        );
        assertEquals(EXPECTED_PYTH_VERSION, version);

        // Next mapping account
        final PublicKey nextMappingAccount = mappingAccount.getNextMappingAccount();
        assertNull(nextMappingAccount);
    }

    @Test
    public void productAccountTest() {
        final MappingAccount mappingAccount = pythManager.getMappingAccount(MAPPING_ACCOUNT);
        final PublicKey productAccountKey = mappingAccount.getProductAccountKeys().get(0);

        assertNotNull(productAccountKey);

        final ProductAccount productAccount = pythManager.getProductAccount(productAccountKey);
        LOGGER.info(
                String.format(
                        "Product Account = %s",
                        productAccount.toString()
                )
        );

        int magicNumber = productAccount.getMagicNumber();

        assertEquals(PYTH_MAGIC_NUMBER, magicNumber);
        assertTrue(productAccount.getProductAttributes().size() > 0);
    }

    @Test
    @Disabled
    public void priceDataAccountTest() {
        final int EXPECTED_MIN_BCH_USD_PUBLISHERS = 3;
        final MappingAccount mappingAccount = pythManager.getMappingAccount(MAPPING_ACCOUNT);
        final PublicKey productAccountKey = mappingAccount.getProductAccountKeys().get(0);

        assertNotNull(productAccountKey);

        final ProductAccount productAccount = pythManager.getProductAccount(productAccountKey);
        LOGGER.info(
                String.format(
                        "Product Account = %s",
                        productAccount.toString()
                )
        );
        final PriceDataAccount priceDataAccount = pythManager.getPriceDataAccount(productAccount.getPriceAccountKey());
        LOGGER.info(
                String.format(
                        "Price Data Account = %s",
                        priceDataAccount.toString()
                )
        );

        assertEquals(EXPECTED_EXPONENT, priceDataAccount.getExponent());

        LOGGER.info(
                String.format(
                        "priceComponents = %s",
                        priceDataAccount.getPriceComponents()
                )
        );

        LOGGER.info(
                String.format(
                        "Minimum publishers = %d",
                        priceDataAccount.getMinPublishers()
                )
        );
        assertTrue(priceDataAccount.getMinPublishers() >= EXPECTED_MIN_BCH_USD_PUBLISHERS);

        LOGGER.info(
                String.format(
                        "drv1 = %.4f, drv2 = %d, drv3 = %d, drv4 = %d",
                        priceDataAccount.getDrv1(),
                        priceDataAccount.getDrv2(),
                        priceDataAccount.getDrv3(),
                        priceDataAccount.getDrv4()
                )
        );

        assertEquals(0.0f, priceDataAccount.getDrv2(), 0.0);
    }

    @Test
    @Disabled
    public void mainnetTest() throws InterruptedException {
        final Map<String, Float> currentPriceMap = new ConcurrentHashMap<>();

        final MappingAccount mappingAccount = pythManager.getMappingAccount(MAPPING_ACCOUNT);
        Thread.sleep(100L);
        final List<PublicKey> productAccountKeys = mappingAccount.getProductAccountKeys();
        for (PublicKey productAccountKey : productAccountKeys) {
            final ProductAccount productAccount = pythManager.getProductAccount(productAccountKey);
            LOGGER.info(
                    String.format(
                            "Asset: %s",
                            productAccount.getProductAttributes().get("description")
                    )
            );
            Thread.sleep(100L);
            final PublicKey priceDataAccountKey = productAccount.getPriceAccountKey();
            webSocketClient.accountSubscribe(
                    priceDataAccountKey.toBase58(),
                    new PriceDataAccountListener(
                            currentPriceMap,
                            productAccount.getProductAttributes().get("symbol")
                    )
            );

            LOGGER.info(
                    String.format(
                            "Subscribed to %s",
                            priceDataAccountKey.toBase58()
                    )
            );
            Thread.sleep(2000L);
        }

        Thread.sleep(60000L);
    }

    @Test
    @Disabled
    public void iterateProductsTest() throws InterruptedException {
        // Iterate all products and get their price once
        final MappingAccount mappingAccount = pythManager.getMappingAccount(MAPPING_ACCOUNT);
        Thread.sleep(100L);

        final List<PublicKey> productAccountKeys = mappingAccount.getProductAccountKeys();
        for (PublicKey productAccountKey : productAccountKeys) {
            final ProductAccount productAccount = pythManager.getProductAccount(productAccountKey);
            final PublicKey priceDataAccountKey = productAccount.getPriceAccountKey();
            Thread.sleep(200L);

            final PriceDataAccount priceDataAccount = pythManager.getPriceDataAccount(priceDataAccountKey);
            Thread.sleep(300L);

            LOGGER.info(
                    String.format(
                            "Asset: %s, Price: %.2f",
                            productAccount.getProductAttributes().get("description"),
                            priceDataAccount.getAggregatePriceInfo().getPrice()
                    )
            );
        }
    }
}
