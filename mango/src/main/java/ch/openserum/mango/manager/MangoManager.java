package ch.openserum.mango.manager;

import ch.openserum.mango.model.MangoPerpGroup;
import ch.openserum.mango.model.MangoGroup;
import ch.openserum.mango.model.MarginAccount;
import lombok.RequiredArgsConstructor;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        byte[] mangoGroupData = getAccountData(publicKey);
        return MangoGroup.readMangoGroup(mangoGroupData);
    }

    public MarginAccount getMarginAccount(final PublicKey publicKey, final PublicKey dexProgramId) {
        // Decode Margin Account
        byte[] marginAccountData = getAccountData(publicKey);
        final MarginAccount marginAccount = MarginAccount.readMarginAccount(publicKey, marginAccountData);

        // Populate marginAccount with Open Orders
        marginAccount.loadOpenOrders(dexProgramId);

        return marginAccount;
    }

    private byte[] getAccountData(final PublicKey publicKey) {
        AccountInfo accountInfo = null;

        try {
            accountInfo = client.getApi().getAccountInfo(publicKey);
        } catch (RpcException e) {
            LOGGER.warning(e.getMessage());
        }

        if (accountInfo == null) {
            return null;
        }

        return Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
    }

    public MangoPerpGroup getMangoPerpGroup(final PublicKey publicKey) {
        byte[] mangoPerpGroupData = getAccountData(publicKey);

        try {
            Files.write(Path.of("mangoPerpGroup.bin"), mangoPerpGroupData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return MangoPerpGroup.readMangoPerpGroup(publicKey, mangoPerpGroupData);
    }

}
