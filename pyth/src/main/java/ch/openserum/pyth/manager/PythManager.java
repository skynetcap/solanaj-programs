package ch.openserum.pyth.manager;

import ch.openserum.pyth.model.MappingAccount;
import ch.openserum.pyth.utils.PythUtils;
import lombok.RequiredArgsConstructor;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;

import java.util.Base64;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class PythManager {

    private final RpcClient client;
    private static final Logger LOGGER = Logger.getLogger(PythManager.class.getName());

    private static final int MAGIC_NUMBER_OFFSET = 0;
    private static final int VERSION_OFFSET = MAGIC_NUMBER_OFFSET + PythUtils.INT32_SIZE;


    public MappingAccount getMappingAccount(final PublicKey publicKey) {
        byte[] data = getAccountData(publicKey);

        final MappingAccount mappingAccount = MappingAccount.builder()
                .magicNumber(PythUtils.readInt32(data, MAGIC_NUMBER_OFFSET))
                .version(PythUtils.readInt32(data, VERSION_OFFSET))
                .build();

        return mappingAccount;
    }

    // TODO Deduplicate this with MangoManager
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
}
