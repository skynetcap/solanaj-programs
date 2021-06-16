import ch.openserum.mango.model.MangoGroup;
import ch.openserum.mango.model.MangoGroupAccountFlags;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

        try {
            Files.write(Path.of("mangoGroup.bin"), mangoGroupData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
