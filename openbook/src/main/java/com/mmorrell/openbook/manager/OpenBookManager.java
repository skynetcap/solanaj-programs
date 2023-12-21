package com.mmorrell.openbook.manager;

import com.mmorrell.openbook.OpenBookUtil;
import com.mmorrell.openbook.model.BookSide;
import com.mmorrell.openbook.model.LeafNode;
import com.mmorrell.openbook.model.OpenBookEventHeap;
import com.mmorrell.openbook.model.OpenBookMarket;
import com.mmorrell.openbook.model.OpenBookOrder;
import com.mmorrell.openbook.program.OpenbookProgram;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ProgramAccount;
import org.p2p.solanaj.rpc.types.config.Commitment;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class OpenBookManager {

    private final RpcClient client;
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

    public List<OpenBookMarket> getOpenBookMarkets() {
        return marketCache.values().stream().toList();
    }

    public Optional<OpenBookMarket> getMarket(PublicKey marketId, boolean useCache, boolean retrieveOrderBooks) {
        if (useCache) {
            if (marketCache.containsKey(marketId)) {
                return Optional.of(marketCache.get(marketId));
            } else {
                return Optional.empty();
            }
        } else {
            try {
                OpenBookMarket openBookMarket = OpenBookMarket.readOpenBookMarket(
                        client.getApi()
                                .getAccountInfo(marketId, Map.of("commitment", Commitment.PROCESSED))
                                .getDecodedData(),
                        marketId
                );

                if (retrieveOrderBooks) {
                    BookSide bids = BookSide.readBookSide(
                            client.getApi()
                                    .getAccountInfo(openBookMarket.getBids(), Map.of("commitment", Commitment.PROCESSED))
                                    .getDecodedData()
                    );
                    bids.setBaseDecimals(openBookMarket.getBaseDecimals());
                    bids.setQuoteDecimals(openBookMarket.getQuoteDecimals());
                    bids.setBaseLotSize(openBookMarket.getBaseLotSize());
                    bids.setQuoteLotSize(openBookMarket.getQuoteLotSize());

                    BookSide asks = BookSide.readBookSide(
                            client.getApi()
                                    .getAccountInfo(openBookMarket.getAsks(), Map.of("commitment", Commitment.PROCESSED))
                                    .getDecodedData()
                    );
                    asks.setBaseDecimals(openBookMarket.getBaseDecimals());
                    asks.setQuoteDecimals(openBookMarket.getQuoteDecimals());
                    asks.setBaseLotSize(openBookMarket.getBaseLotSize());
                    asks.setQuoteLotSize(openBookMarket.getQuoteLotSize());

                    openBookMarket.setBidOrders(bids.getOrders());
                    openBookMarket.setAskOrders(asks.getOrders());
                }

                return Optional.of(openBookMarket);
            } catch (Exception e) {
                log.error("Unable to retrieve OpenBook v2 market {}", marketId);
                return Optional.empty();
            }
        }
    }

    public Optional<OpenBookEventHeap> getEventHeap(PublicKey eventHeap) {
        try {
            OpenBookEventHeap openBookEventHeap = OpenBookEventHeap.readOpenBookEventHeap(
                    client.getApi()
                            .getAccountInfo(eventHeap, Map.of("commitment", Commitment.PROCESSED))
                            .getDecodedData()
            );

            return Optional.of(openBookEventHeap);
        } catch (RpcException e) {
            log.error(e.getMessage());
            return Optional.empty();
        }
    }


}
