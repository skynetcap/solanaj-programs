package ch.openserum.mango.manager;

import ch.openserum.mango.model.MangoGroup;
import lombok.RequiredArgsConstructor;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;

import java.util.Base64;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class MangoManager {

    private final RpcClient client;
    private static final Logger LOGGER = Logger.getLogger(MangoManager.class.getName());
    private static final PublicKey BTC_ETH_SOL_SRM_USDC_MANGO_GROUP =
            new PublicKey("2oogpTYm1sp6LPZAWD3bp2wsFpnV2kXL1s52yyFhW5vp");

    public MangoGroup getDefaultMangoGroup() {
        return getMangoGroup(BTC_ETH_SOL_SRM_USDC_MANGO_GROUP);
    }

    public MangoGroup getMangoGroup(final PublicKey publicKey) {
        AccountInfo accountInfo = null;

        try {
            accountInfo = client.getApi().getAccountInfo(publicKey);
        } catch (RpcException e) {
            LOGGER.warning(e.getMessage());
        }

        if (accountInfo == null) {
            return null;
        }

        byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
        return MangoGroup.readMangoGroup(data);
    }

}
