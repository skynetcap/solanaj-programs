package com.mmorrell.openbook.manager;

import com.mmorrell.openbook.OpenBookUtil;
import com.mmorrell.openbook.model.OpenBookMarket;
import com.mmorrell.openbook.program.OpenbookProgram;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ProgramAccount;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class OpenBookManager {

    private RpcClient client;

    @Getter
    private final Map<PublicKey, OpenBookMarket> marketCache = new HashMap<>();

    public OpenBookManager(RpcClient client) {
        this.client = client;
        cacheMarkets();
    }

    public void cacheMarkets() {
        final List<ProgramAccount> markets;
        try {
            markets = client.getApi().getProgramAccountsBase64(
                    OpenbookProgram.OPENBOOK_V2_PROGRAM_ID,
                    0,
                    Base58.encode(OpenBookUtil.MARKET_DISCRIMINATOR)
            );
        } catch (RpcException e) {
            log.error("Error caching OpenBook v2 markets: {}", e.getMessage());
            return;
        }

        markets.forEach(programAccount -> {
            OpenBookMarket openBookMarket = OpenBookMarket.readOpenBookMarket(
                    programAccount.getAccount().getDecodedData(),
                    new PublicKey(programAccount.getPubkey())
            );
            marketCache.put(openBookMarket.getMarketId(), openBookMarket);
        });
    }


}
