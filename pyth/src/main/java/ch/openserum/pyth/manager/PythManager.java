package com.mmorrell.pyth.manager;

import com.mmorrell.pyth.model.MappingAccount;
import com.mmorrell.pyth.model.PriceDataAccount;
import com.mmorrell.pyth.model.ProductAccount;
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

    public MappingAccount getMappingAccount(final PublicKey publicKey) {
        byte[] data = getAccountData(publicKey);
        return MappingAccount.readMappingAccount(data);
    }

    public ProductAccount getProductAccount(final PublicKey publicKey) {
        byte[] data = getAccountData(publicKey);
        return ProductAccount.readProductAccount(data);
    }

    public PriceDataAccount getPriceDataAccount(final PublicKey publicKey) {
        byte[] data = getAccountData(publicKey);
        return PriceDataAccount.readPriceDataAccount(data);
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
