import com.mmorrell.bonfida.manager.NamingManager;
import org.bitcoinj.core.Utils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.Filter;
import org.p2p.solanaj.rpc.types.Memcmp;
import org.p2p.solanaj.rpc.types.ProgramAccount;
import org.p2p.solanaj.rpc.types.config.ProgramAccountConfig;
import org.p2p.solanaj.rpc.types.config.RpcSendTransactionConfig;
import org.p2p.solanaj.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

public class BonfidaTest {

    private static final Logger LOGGER = Logger.getLogger(BonfidaTest.class.getName());
    private final NamingManager namingManager = new NamingManager(new RpcClient(Cluster.MAINNET));
    private static final String DOMAIN_NAME = ".sol";  // testdomainname.sol
    private final PublicKey skynetMainnetPubkey = new PublicKey("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq");
    private final PublicKey bonfidaPubkey = new PublicKey("jCebN34bUfdeUYJT13J1yG16XWQpt5PDx6Mse9GUqhR");
    private final RpcClient rpcClient = new RpcClient("https://solana-api.projectserum.com/");
    private static final PublicKey NAME_PROGRAM_ID = new PublicKey("namesLPneVptA9Z5rqUDD9tMTWEJwofgaYwp8cawRkX");

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        // Prevent RPCPool rate limit
        Thread.sleep(2000L);
    }

    @Test
    @Ignore
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
    public void resolveTest() throws Exception {
        LOGGER.info("Looking up domains for: " + skynetMainnetPubkey.toBase58());
        PublicKey.ProgramDerivedAddress centralState = PublicKey.findProgramAddress(
                List.of(
                        bonfidaPubkey.toByteArray()
                ),
                bonfidaPubkey
        );

        // find named accounts for user
        ProgramAccountConfig config = new ProgramAccountConfig(
                List.of(
                        new Filter(new Memcmp(32, skynetMainnetPubkey.toBase58()))
                )
        );
        config.setEncoding(RpcSendTransactionConfig.Encoding.base64);

        List<ProgramAccount> programAccounts = rpcClient.getApi().getProgramAccounts(NAME_PROGRAM_ID, config);
        for (ProgramAccount programAccount : programAccounts) {
            byte[] hashedReverseLookup = namingManager.getHashedName(programAccount.getPubkey());
            PublicKey nameAccountKey = PublicKey.findProgramAddress(Arrays.asList(hashedReverseLookup,
                            centralState.getAddress().toByteArray(),
                            ByteBuffer.allocate(32).array()),
                    NAME_PROGRAM_ID).getAddress();

            try {
                byte[] data =
                        Base64.getDecoder().decode(rpcClient.getApi().getAccountInfo(nameAccountKey).getValue().getData().get(0));

                // bytes
                LOGGER.info(String.format("Name account key: %s", nameAccountKey.toBase58()));
                LOGGER.info(String.format("Data: %s", Arrays.toString(data)));

                int nameLength = (int) Utils.readUint32(data, 96);
                String domainName = new String(ByteUtils.readBytes(data, 96 + 4, nameLength));

                LOGGER.info("Domain name for " + skynetMainnetPubkey.toBase58() + ", = " + domainName + ".sol");
            } catch (Exception ex) {
                // LOGGER.info("no info found..");
            }
        }
    }
}
