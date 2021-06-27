package ch.openserum.pyth.manager;

import ch.openserum.pyth.model.MappingAccount;
import ch.openserum.pyth.model.PriceDataAccount;
import ch.openserum.pyth.model.PriceInfoAccount;
import ch.openserum.pyth.model.ProductAccount;
import ch.openserum.pyth.utils.PythUtils;
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
public class PythManager {

    private final RpcClient client;
    private static final Logger LOGGER = Logger.getLogger(PythManager.class.getName());

    public MappingAccount getMappingAccount(final PublicKey publicKey) {
        byte[] data = getAccountData(publicKey);

        try {
            Files.write(Path.of("mappingAccount.bin"), data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return MappingAccount.readMappingAccount(data);
    }

    public ProductAccount getProductAccount(final PublicKey publicKey) {
        byte[] data = getAccountData(publicKey);

        try {
            Files.write(Path.of("productAccount.bin"), data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ProductAccount.readProductAccount(data);
    }

    public PriceInfoAccount getPriceInfoAccount(final PublicKey publicKey) {
        byte[] data = getAccountData(publicKey);

        try {
            Files.write(Path.of("priceInfoAccount.bin"), data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return PriceInfoAccount.readPriceInfoAccount(data);
    }

    public PriceDataAccount getPriceDataAccount(final PublicKey publicKey) {
        byte[] data = getAccountData(publicKey);

        try {
            Files.write(Path.of("priceDataAccount.bin"), data);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
