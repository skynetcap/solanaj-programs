package com.mmorrell.jupiter.manager;

import com.mmorrell.jupiter.model.*;
import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;

import java.util.Base64;
import java.util.Optional;

@Slf4j
public class JupiterManager {

    private final RpcClient client;
    private static final PublicKey JUPITER_PROGRAM_ID = new PublicKey("PERPHjGBqRHArX4DySjwM6UJHiR3sWAatqfdBS2qQJu");

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
}
