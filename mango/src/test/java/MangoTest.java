import ch.openserum.mango.manager.MangoManager;
import ch.openserum.mango.model.MangoAccount;
import ch.openserum.mango.model.MangoGroup;
import ch.openserum.mango.model.MangoIndex;
import ch.openserum.mango.model.MarginAccount;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;

import java.util.Base64;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

public class MangoTest {

    private final RpcClient client = new RpcClient(Cluster.MAINNET);
    private final RpcClient devnetClient = new RpcClient("https://api.devnet.solana.com");
    private final MangoManager mangoManager = new MangoManager(client);
    private final MangoManager devnetMangoManager = new MangoManager(devnetClient);
    private static final Logger LOGGER = Logger.getLogger(MangoTest.class.getName());
    private static final PublicKey BTC_ETH_SOL_SRM_USDC_MANGO_GROUP =
            new PublicKey("2oogpTYm1sp6LPZAWD3bp2wsFpnV2kXL1s52yyFhW5vp");

    @Test
    public void mangoGroupTest() throws RpcException {
        LOGGER.info(
                String.format(
                        "Looking up Mango Group %s (%s)",
                        "BTC_ETH_SOL_SRM_USDC",
                        BTC_ETH_SOL_SRM_USDC_MANGO_GROUP
                )
        );

        // Retrieve MangoGroup from Solana
        final AccountInfo mangoGroupAccountInfo = client.getApi().getAccountInfo(
                BTC_ETH_SOL_SRM_USDC_MANGO_GROUP
        );

        // Deserialize MangoGroup
        byte[] mangoGroupData = Base64.getDecoder().decode(mangoGroupAccountInfo.getValue().getData().get(0));
        final MangoGroup mangoGroup = MangoGroup.readMangoGroup(mangoGroupData);

        LOGGER.info(
                String.format(
                        "Mango Group: Initialized (%b), MangoGroup (%b), MarginAccount (%b), MangoSrmAccount (%b)",
                        mangoGroup.getAccountFlags().isInitialized(),
                        mangoGroup.getAccountFlags().isMangoGroup(),
                        mangoGroup.getAccountFlags().isMarginAccount(),
                        mangoGroup.getAccountFlags().isMangoSrmAccount()
                )
        );

        // Deposits and Borrow indexes test
        for (int i = 0; i < 5; i++) {
            MangoIndex mangoIndex = mangoGroup.getIndexes().get(i);
            LOGGER.info(
                    String.format(
                            "Token Index %d (%s): %s",
                            i,
                            mangoGroup.getTokens().get(i).toBase58(),
                            mangoIndex.toString()
                    )
            );

            LOGGER.info(
                    String.format(
                            "Deposit = %.16f, Borrow = %.16f",
                            mangoIndex.getDeposit().decodeFloat(),
                            mangoIndex.getBorrow().decodeFloat()
                    )
            );
        }

        LOGGER.info(
                String.format(
                        "signerNonce = %d, signerKey = %s",
                        mangoGroup.getSignerNonce(),
                        mangoGroup.getSignerKey()
                )
        );

        LOGGER.info(
                String.format(
                        "DEX Program ID = %s",
                        mangoGroup.getDexProgramId().toBase58()
                )
        );

        // Total Deposits
        mangoGroup.getTotalDeposits().forEach(totalDeposit -> {
            LOGGER.info(
                    String.format(
                            "Total Deposit = %.2f",
                            totalDeposit.decodeFloat()
                    )
            );
        });

        // Total Borrows
        mangoGroup.getTotalBorrows().forEach(totalBorrow -> {
            LOGGER.info(
                    String.format(
                            "Total Borrow = %.2f",
                            totalBorrow.decodeFloat()
                    )
            );
        });

        LOGGER.info(
                String.format(
                        "maintCollRatio = %.2f, initCollRatio = %.2f",
                        mangoGroup.getMaintCollRatio().decodeFloat(),
                        mangoGroup.getInitCollRatio().decodeFloat()
                )
        );

        LOGGER.info(
                String.format(
                        "srmVault = %s, admin = %s",
                        mangoGroup.getSrmVault(),
                        mangoGroup.getAdmin()
                )
        );

        // Borrow Limits
        mangoGroup.getBorrowLimits().forEach(borrowLimit -> {
            LOGGER.info(
                    String.format(
                            "Borrow limit = %d",
                            borrowLimit
                    )
            );
        });
    }

    @Test
    public void getDefaultMangoGroupTest() {
        final MangoGroup defaultMangoGroup = mangoManager.getDefaultMangoGroup();

        LOGGER.info(
                String.format(
                        "defaultMangoGroup = %s",
                        defaultMangoGroup.toString()
                )
        );
    }

    @Test
    public void getMangoGroupAndMarginAccountTest() {
        final PublicKey marginAccountPk = PublicKey.valueOf("E95EaroWhpRnEtCpjH8HsFnzrQdcwxsXtVPwS8BbsXE");
        final MangoGroup defaultMangoGroup = mangoManager.getDefaultMangoGroup();
        final MarginAccount marginAccount = mangoManager.getMarginAccount(
                marginAccountPk,
                defaultMangoGroup.getDexProgramId()
        );

        LOGGER.info(
                String.format(
                        "Mango Group = %s",
                        defaultMangoGroup
                )
        );

        LOGGER.info(
                String.format(
                        "Margin Account = %s",
                        marginAccount.toString()
                )
        );


        for (int i = 0; i < 5; i++) {
            LOGGER.info(
                    String.format(
                            "Deposit = %.8f, BigDecimal = %s",
                            marginAccount.getDeposits().get(i).decodeFloat() /
                                    Math.pow(10, defaultMangoGroup.getMintDecimals().get(i)),
                            marginAccount.getDeposits().get(i).decodeBigDecimal()
                    )
            );
        }

        assertTrue(marginAccount.getAccountFlags().isMarginAccount());
    }

    @Test
    public void mangoV3Test() {
        final MangoAccount mangoAccount = devnetMangoManager.getMangoAccount(
                PublicKey.valueOf("ECAikQUnS8HGLnzGrqEYA6Daz8nRRu9GsbfLbwMfK23P")
        );

        LOGGER.info(
                String.format(
                        "Mango Account = %s",
                        mangoAccount
                )
        );
    }
}
