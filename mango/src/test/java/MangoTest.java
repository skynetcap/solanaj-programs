import ch.openserum.mango.model.MangoGroup;
import ch.openserum.mango.model.MangoIndex;
import ch.openserum.mango.model.U64F64;
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
    private static final PublicKey SKYNET =
            new PublicKey("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq");
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

        final AccountInfo mangoGroupAccountInfo = client.getApi().getAccountInfo(
                BTC_ETH_SOL_SRM_USDC_MANGO_GROUP
        );

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

            U64F64 deposit = new U64F64(mangoIndex.getDeposit());
            U64F64 borrow = new U64F64(mangoIndex.getBorrow());

            LOGGER.info(String.format("Deposit = %.16f, Borrow = %.16f", deposit.decode(), borrow.decode()));
        }
    }
}
