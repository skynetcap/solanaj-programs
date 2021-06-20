import ch.openserum.mango.model.MangoGroup;
import ch.openserum.mango.model.MangoIndex;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;

import java.util.Base64;
import java.util.logging.Logger;

public class MangoTest {

    private final RpcClient client = new RpcClient(Cluster.MAINNET);
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
                            mangoIndex.getDeposit().decode(),
                            mangoIndex.getBorrow().decode()
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
                            totalDeposit.decode()
                    )
            );
        });

        // Total Borrows
        mangoGroup.getTotalBorrows().forEach(totalBorrow -> {
            LOGGER.info(
                    String.format(
                            "Total Borrow = %.2f",
                            totalBorrow.decode()
                    )
            );
        });

        LOGGER.info(
                String.format(
                        "maintCollRatio = %.2f, initCollRatio = %.2f",
                        mangoGroup.getMaintCollRatio().decode(),
                        mangoGroup.getInitCollRatio().decode()
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
}
