package com.mmorrell.phoenix.manager;

import com.mmorrell.phoenix.model.PhoenixMarketHeader;
import com.mmorrell.phoenix.program.PhoenixProgram;
import com.mmorrell.phoenix.util.PhoenixUtil;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ProgramAccount;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Data
@Builder
@Slf4j
public class PhoenixManager {

    private RpcClient rpcClient;
    private Set<PhoenixMarketHeader> phoenixMarketHeaders;
    private Set<PublicKey> marketIds;

    public List<PhoenixMarketHeader> getPhoenixMarketHeaders() {
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
            marketIds.add(new PublicKey(programAccount.getPubkey()));
            final PhoenixMarketHeader phoenixMarketHeader = PhoenixMarketHeader.readPhoenixMarketHeader(
                    Arrays.copyOfRange(
                            programAccount.getAccount().getDecodedData(),
                            0,
                            PhoenixMarketHeader.MARKET_HEADER_SIZE
                    )
            );
            phoenixMarketHeaders.add(phoenixMarketHeader);
        });

        return phoenixMarketHeaders.stream().toList();
    }


}
