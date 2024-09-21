import com.mmorrell.bonfida.manager.NamingManager;
import org.junit.jupiter.api.*;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.types.AccountInfo;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BonfidaTest {

    private static final Logger LOGGER = Logger.getLogger(BonfidaTest.class.getName());
    private final RpcClient rpcClient = new RpcClient(Cluster.MAINNET);
    private final NamingManager namingManager = new NamingManager(rpcClient);
    private final PublicKey skynetMainnetPubkey = new PublicKey("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq");

    @BeforeAll
    public static void beforeClass() throws InterruptedException {
        // Prevent RPCPool rate limit
        Thread.sleep(2000L);
    }

    @Test
    public void retrieveNameFromRegistry() {
        // getAccountInfo
        AccountInfo testAccountInfo = namingManager.getAccountInfo(new PublicKey("BVk1qg1y9AJ3LkfWCpr8FkDXZZcu7muAyVgbTBDbqDwZ"));
        byte[] data = Base64.getDecoder().decode(testAccountInfo.getValue().getData().get(0));

        LOGGER.info(Arrays.toString(data));


        PublicKey parentName = PublicKey.readPubkey(data, 0);
        PublicKey owner = PublicKey.readPubkey(data, 32);
        PublicKey nameClass = PublicKey.readPubkey(data, 64);
        byte[] nameData = Arrays.copyOfRange(data, 64, data.length);


        LOGGER.info(String.format("parentName = %s, owner = %s, nameClass = %s", parentName, owner, nameClass));
        LOGGER.info(String.format("data = %s", Arrays.toString(nameData)));
    }

    @Test
    public void getTwitterHandleTest() {
        String twitterHandle = namingManager.getTwitterHandle(skynetMainnetPubkey);

        LOGGER.info(twitterHandle);
        assertTrue(twitterHandle.equalsIgnoreCase("skynetcap"));
    }

    @Test
    public void twitterHandleToPubkeyLookupTest() {
        PublicKey pubkey = namingManager.getPublicKey("SBF_Alameda");

        LOGGER.info(pubkey.toBase58());
    }

    @Test
    public void getPublicKeyBySolDomainTest() {
        PublicKey publicKey = namingManager.getPublicKeyBySolDomain("skynet");
        LOGGER.info(
                String.format(
                        "skynet.sol = %s",
                        publicKey.toBase58()
                )
        );

        assertTrue(publicKey.toBase58().equalsIgnoreCase("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq"));

        PublicKey sbfPublicKey = namingManager.getPublicKeyBySolDomain("sbf");
        LOGGER.info(
                String.format(
                        "sbf.sol = %s",
                        sbfPublicKey.toBase58()
                )
        );

        assertTrue(sbfPublicKey.toBase58().equalsIgnoreCase("2NoEcR9cC7Rn6bP9rBpky6B1eP9syyPf8FXRaf1myChv"));
    }

    @Test
    @Disabled
    public void resolveTest() {
        LOGGER.info("Looking up domain for: " + skynetMainnetPubkey.toBase58());
        Optional<String> domainName = namingManager.getDomainNameByPubkey(skynetMainnetPubkey);

        // Verify we got a domain
        assertTrue(domainName.isPresent());

        String fullDomain = domainName.get() + ".sol";
        LOGGER.info("Domain = " + fullDomain);

        // Verify it matches skynet.sol
        assertEquals("skynet.sol", fullDomain);

        // Reverse lookup on SBF's wallet
        Optional<String> sbfWallet = namingManager.getDomainNameByPubkey(
                new PublicKey("2NoEcR9cC7Rn6bP9rBpky6B1eP9syyPf8FXRaf1myChv")
        );

        assertTrue(sbfWallet.isPresent());
    }
}
