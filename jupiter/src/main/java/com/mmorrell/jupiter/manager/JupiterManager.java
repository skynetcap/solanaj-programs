package com.mmorrell.jupiter.manager;

import com.mmorrell.jupiter.model.*;
import com.mmorrell.jupiter.util.JupiterUtil;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.Memcmp;
import org.p2p.solanaj.rpc.types.ProgramAccount;

import java.util.*;

@Slf4j
public class JupiterManager {

    private final RpcClient client;
    private static final PublicKey JUPITER_PROGRAM_ID = new PublicKey("PERPHjGBqRHArX4DySjwM6UJHiR3sWAatqfdBS2qQJu");
    private static final String DCA_PROGRAM_ID = "DCA265Vj8a9CEuX1eb1LWRnDT7uK6q1xMipnNyatn23M"; // Replace with actual DCA Program ID
    private static final int DCA_ACCOUNT_SIZE = 289; // Updated based on JupiterDca structure

    public JupiterManager() {
        this.client = new RpcClient(Cluster.MAINNET);
    }

    public JupiterManager(RpcClient client) {
        this.client = client;
    }

    public Optional<JupiterPerpPosition> getPosition(PublicKey positionPublicKey) {
        try {
            AccountInfo accountInfo = client.getApi().getAccountInfo(positionPublicKey);
            if (accountInfo == null || accountInfo.getValue() == null) {
                return Optional.empty();
            }
            byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
            return Optional.of(JupiterPerpPosition.fromByteArray(data));
        } catch (RpcException e) {
            log.warn("Error fetching position: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<JupiterPool> getPool(PublicKey poolPublicKey) {
        try {
            AccountInfo accountInfo = client.getApi().getAccountInfo(poolPublicKey);
            if (accountInfo == null || accountInfo.getValue() == null) {
                return Optional.empty();
            }
            byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
            return Optional.of(JupiterPool.fromByteArray(data));
        } catch (RpcException e) {
            log.warn("Error fetching pool: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<JupiterCustody> getCustody(PublicKey custodyPublicKey) {
        try {
            AccountInfo accountInfo = client.getApi().getAccountInfo(custodyPublicKey);
            if (accountInfo == null || accountInfo.getValue() == null) {
                return Optional.empty();
            }
            byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
            return Optional.of(JupiterCustody.fromByteArray(data));
        } catch (RpcException e) {
            log.warn("Error fetching custody: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<JupiterPositionRequest> getPositionRequest(PublicKey positionRequestPublicKey) {
        try {
            AccountInfo accountInfo = client.getApi().getAccountInfo(positionRequestPublicKey);
            if (accountInfo == null || accountInfo.getValue() == null) {
                return Optional.empty();
            }
            byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
            return Optional.of(JupiterPositionRequest.fromByteArray(data));
        } catch (RpcException e) {
            log.warn("Error fetching position request: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<JupiterPerpetuals> getPerpetuals(PublicKey perpetualsPublicKey) {
        try {
            AccountInfo accountInfo = client.getApi().getAccountInfo(perpetualsPublicKey);
            if (accountInfo == null || accountInfo.getValue() == null) {
                return Optional.empty();
            }
            byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
            return Optional.of(JupiterPerpetuals.fromByteArray(data));
        } catch (RpcException e) {
            log.warn("Error fetching perpetuals: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves all Jupiter DCA accounts.
     *
     * @return a list of JupiterDca objects.
     * @throws RpcException if the RPC call fails.
     */
    public List<JupiterDca> getAllDcaAccounts() {
        PublicKey programId = new PublicKey(DCA_PROGRAM_ID);

        byte[] dcaDiscriminator = JupiterUtil.getAccountDiscriminator("Dca");

        // Create a memcmp filter for the discriminator at offset 0
        Memcmp memCmpFilter = new Memcmp(0, Base58.encode(dcaDiscriminator));

        try {
            List<ProgramAccount> accounts = client.getApi().getProgramAccounts(
                    programId,
                    List.of(memCmpFilter),
                    DCA_ACCOUNT_SIZE
            );

            List<JupiterDca> dcaAccounts = new ArrayList<>();
            for (ProgramAccount account : accounts) {
                byte[] data = account.getAccount().getDecodedData();
                JupiterDca dca = JupiterDca.fromByteArray(data);
                dcaAccounts.add(dca);
            }

            return dcaAccounts;
        } catch (RpcException ex) {
            log.warn("Error fetching DCA accounts: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }
}