import ch.openserum.pyth.listener.PriceDataAccountListener;
import ch.openserum.pyth.manager.PythManager;
import ch.openserum.pyth.model.MappingAccount;
import ch.openserum.pyth.model.PriceDataAccount;
import ch.openserum.pyth.model.ProductAccount;
import org.junit.Ignore;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.ws.SubscriptionWebSocketClient;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class DevnetTest {

    private final String ENDPOINT = "https://api.devnet.solana.com";
    private final RpcClient client = new RpcClient(ENDPOINT);
    private final SubscriptionWebSocketClient webSocketClient = SubscriptionWebSocketClient.getInstance(ENDPOINT);
    private final PythManager pythManager = new PythManager(client);
    private static final Logger LOGGER = Logger.getLogger(DevnetTest.class.getName());
    private static final int PYTH_MAGIC_NUMBER = (int) Long.parseLong("a1b2c3d4", 16);
    private static final int EXPECTED_PYTH_VERSION = 2;
    private static final int EXPECTED_EXPONENT = -9;
    private static final PublicKey TEST_MAPPING_ACCOUNT = PublicKey.valueOf(
            "BmA9Z6FjioHJPpjT39QazZyhDRUdZy2ezwx4GiDdE2u2"
    );
    private static final int EUR_USD_PRODUCT_ACCOUNT_KEY_INDEX = 20;

    @Test
    public void mappingAccount() {
        final MappingAccount mappingAccount = pythManager.getMappingAccount(TEST_MAPPING_ACCOUNT);

        LOGGER.info(
                String.format(
                        "Mapping Account = %s",
                        mappingAccount.toString()
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
        final MappingAccount mappingAccount = pythManager.getMappingAccount(TEST_MAPPING_ACCOUNT);
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

        final PublicKey priceAccountKey = productAccount.getPriceAccountKey();
        assertTrue(
                priceAccountKey.toBase58().equalsIgnoreCase("4EQrNZYk5KR1RnjyzbaaRbHsv8VqZWzSUtvx58wLsZbj")
        );

        assertTrue(productAccount.getProductAttributes().size() > 0);
    }

    @Test
    public void priceDataAccountTest() {
        final MappingAccount mappingAccount = pythManager.getMappingAccount(TEST_MAPPING_ACCOUNT);
        final PublicKey productAccountKey = mappingAccount.getProductAccountKeys().get(
                EUR_USD_PRODUCT_ACCOUNT_KEY_INDEX
        );

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
                        "drv1 = %.4f, drv2 = %.4f, drv3 = %.4f",
                        priceDataAccount.getDrv1(),
                        priceDataAccount.getDrv2(),
                        priceDataAccount.getDrv3()
                )
        );

        assertEquals(0.0f, priceDataAccount.getDrv1(), 0.0);
    }

    @Test
    @Ignore
    public void priceDataAccountWebsocketTest() throws InterruptedException {
        final PublicKey DOGE_USD_PUBKEY = PublicKey.valueOf("4L6YhY8VvUgmqG5MvJkUJATtzB2rFqdrJwQCmFLv4Jzy");
        final PriceDataAccount priceDataAccount = pythManager.getPriceDataAccount(DOGE_USD_PUBKEY);
        LOGGER.info(
                String.format(
                        "Price Data Account = %s",
                        priceDataAccount.toString()
                )
        );

        webSocketClient.accountSubscribe(DOGE_USD_PUBKEY.toBase58(), new PriceDataAccountListener(DOGE_USD_PUBKEY));
        Thread.sleep(30000L);
    }

    @Test
    @Ignore
    public void multiplePriceDataAccountWebsocketTest() throws InterruptedException {
        final MappingAccount mappingAccount = pythManager.getMappingAccount(TEST_MAPPING_ACCOUNT);
        Thread.sleep(100L);
        final List<PublicKey> productAccountKeys = mappingAccount.getProductAccountKeys();
        for (PublicKey productAccountKey : productAccountKeys) {
            final ProductAccount productAccount = pythManager.getProductAccount(productAccountKey);
            Thread.sleep(100L);
            final PublicKey priceDataAccountKey = productAccount.getPriceAccountKey();
            webSocketClient.accountSubscribe(
                    priceDataAccountKey.toBase58(),
                    new PriceDataAccountListener(priceDataAccountKey)
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
}
