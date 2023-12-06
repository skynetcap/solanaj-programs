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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Data
@Slf4j
public class PhoenixManager {

    private RpcClient rpcClient;
    private Set<PhoenixMarket> phoenixMarkets = new HashSet<>();

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
            log.error("Error caching phoenix market headers: {}", e.getMessage());
        }

        markets.forEach(programAccount -> {
            final PhoenixMarket phoenixMarket = PhoenixMarket.readPhoenixMarket(
                    Arrays.copyOfRange(
                            programAccount.getAccount().getDecodedData(),
                            0,
                            programAccount.getAccount().getDecodedData().length
                    )
            );
            phoenixMarket.setMarketId(new PublicKey(programAccount.getPubkey()));
            phoenixMarkets.add(phoenixMarket);
        });
    }

    public List<PhoenixMarket> getPhoenixMarkets() {
        return phoenixMarkets.stream().toList();
    }

    public Optional<PhoenixMarket> getMarket(PublicKey marketId, boolean useCache) {
        if (useCache) {
            return phoenixMarkets.stream()
                    .filter(market -> market.getMarketId().equals(marketId))
                    .findFirst();
        } else {
            try {
                PhoenixMarket phoenixMarket = PhoenixMarket.readPhoenixMarket(
                        rpcClient.getApi()
                                .getAccountInfo(marketId, Map.of("commitment", Commitment.PROCESSED))
                                .getDecodedData()
                );

                return Optional.of(phoenixMarket);
            } catch (RpcException e) {
                log.error("Unable to retrieve phoenix market {}", marketId);
                return Optional.empty();
            }
        }
    }
}
