package com.mmorrell.phoenix.manager;

import com.mmorrell.phoenix.model.PhoenixMarket;
import com.mmorrell.phoenix.program.PhoenixProgram;
import com.mmorrell.phoenix.util.PhoenixUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ProgramAccount;
import org.p2p.solanaj.rpc.types.config.Commitment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@Slf4j
public class PhoenixManager {

    private final RpcClient rpcClient;
    private final Map<PublicKey, PhoenixMarket> marketCache = new HashMap<>();

    public PhoenixManager(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
        cacheMarkets();
    }

    public void cacheMarkets() {
        List<ProgramAccount> markets = new ArrayList<>();
        try {
            markets = rpcClient.getApi().getProgramAccountsBase64(
                    PhoenixProgram.PHOENIX_PROGRAM_ID,
                    0,
                    PhoenixUtil.getDiscriminator("phoenix::program::accounts::MarketHeader")
            );
        } catch (RpcException e) {
            log.error("Error caching phoenix market headers: {}", e.getMessage(), e);
        }

        markets.forEach(programAccount -> {
            try {
                final PhoenixMarket phoenixMarket = PhoenixMarket.readPhoenixMarket(
                        Arrays.copyOfRange(
                                programAccount.getAccount().getDecodedData(),
                                0,
                                programAccount.getAccount().getDecodedData().length
                        )
                );
                phoenixMarket.setMarketId(new PublicKey(programAccount.getPubkey()));
                marketCache.put(phoenixMarket.getMarketId(), phoenixMarket);
            } catch (Exception ex) {
                log.error("Error reading PHX account: {}", ex.getMessage(), ex);
            }
        });
    }

    public List<PhoenixMarket> getPhoenixMarkets() {
        return marketCache.values().stream().toList();
    }

    public Optional<PhoenixMarket> getMarket(PublicKey marketId, boolean useCache) {
        if (useCache) {
            if (marketCache.containsKey(marketId)) {
                return Optional.of(marketCache.get(marketId));
            } else {
                return Optional.empty();
            }
        } else {
            try {
                PhoenixMarket phoenixMarket = PhoenixMarket.readPhoenixMarket(
                        rpcClient.getApi()
                                .getAccountInfo(marketId, Map.of("commitment", Commitment.PROCESSED))
                                .getDecodedData()
                );
                phoenixMarket.setMarketId(marketId);

                return Optional.of(phoenixMarket);
            } catch (Exception e) {
                log.error("Unable to retrieve phoenix market {}", marketId, e);
                return Optional.empty();
            }
        }
    }
}
