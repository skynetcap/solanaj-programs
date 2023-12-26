package com.mmorrell.openbook.manager;

import com.mmorrell.openbook.OpenBookUtil;
import com.mmorrell.openbook.model.BookSide;
import com.mmorrell.openbook.model.OpenBookEventHeap;
import com.mmorrell.openbook.model.OpenBookFillEvent;
import com.mmorrell.openbook.model.OpenBookMarket;
import com.mmorrell.openbook.model.OpenBookOpenOrdersAccount;
import com.mmorrell.openbook.program.OpenbookProgram;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ProgramAccount;
import org.p2p.solanaj.rpc.types.config.Commitment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The OpenBookManager class is responsible for managing OpenBook markets and their related data.
 */
@Slf4j
public class OpenBookManager {

    private final RpcClient client;
    private final Map<PublicKey, OpenBookMarket> marketCache = new HashMap<>();

    public OpenBookManager(RpcClient client) {
        this.client = client;
        cacheMarkets();
    }

    /**
     * Caches the markets from OpenBook v2 program.
     * It retrieves the markets from the client API and stores them in the market cache.
     */
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

    /**
     * Retrieves a list of open book markets.
     *
     * @return A list of OpenBookMarket objects representing the open book markets.
     */
    public List<OpenBookMarket> getOpenBookMarkets() {
        return marketCache.values().stream().toList();
    }

    /**
     * Retrieves a market from the OpenBook v2 using the provided market ID.
     *
     * @param marketId           the Public Key ID of the market to retrieve
     * @param useCache           flag indicating whether to use the cached market data
     * @param retrieveOrderBooks flag indicating whether to retrieve the order books for the market
     * @return an Optional containing the retrieved OpenBookMarket if successful, or an empty Optional if unsuccessful
     */
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

    /**
     * Retrieves the OpenBookEventHeap associated with the given eventHeap Public Key.
     *
     * @param eventHeap The Public Key of the event heap to retrieve.
     * @return An Optional containing the OpenBookEventHeap if retrieval is successful,
     *         otherwise an empty Optional.
     */
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

    /**
     * Retrieves the OpenBookOpenOrdersAccount associated with the given public key.
     *
     * @param ooa The public key of the OpenBookOpenOrdersAccount to retrieve.
     * @return An Optional containing the OpenBookOpenOrdersAccount if it was successfully retrieved, or an empty Optional if an error occurred.
     */
    public Optional<OpenBookOpenOrdersAccount> getOpenOrdersAccount(PublicKey ooa) {
        try {
            OpenBookOpenOrdersAccount openBookOoa = OpenBookOpenOrdersAccount.readOpenBookOpenOrdersAccount(
                    client.getApi()
                            .getAccountInfo(ooa, Map.of("commitment", Commitment.PROCESSED))
                            .getDecodedData()
            );

            return Optional.of(openBookOoa);
        } catch (RpcException e) {
            log.error("Error getting OOA: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Consume events in an OpenBook market.
     *
     * @param caller     The account making the request.
     * @param marketId   The public key of the market to consume events from.
     * @param limit      The maximum number of events to consume.
     * @return An Optional containing the transaction hash if events are consumed successfully,
     *                   otherwise an empty Optional.
     */
    public Optional<String> consumeEvents(Account caller, PublicKey marketId, long limit) {
        Optional<OpenBookMarket> marketOptional = getMarket(marketId, true, false);
        if (marketOptional.isEmpty()) {
            return Optional.empty();
        }
        OpenBookMarket market = marketOptional.get();

        Optional<OpenBookEventHeap> eventHeapOptional = getEventHeap(market.getEventHeap());
        if (eventHeapOptional.isEmpty()) {
            return Optional.empty();
        }
        OpenBookEventHeap eventHeap = eventHeapOptional.get();
        if (eventHeap.getCount() == 0) {
            return Optional.empty();
        }

        List<PublicKey> peopleToCrank = eventHeap.getFillEvents().stream()
                .map(OpenBookFillEvent::getMaker)
                .toList();

        log.info("Cranking {}: {}", market.getName(), peopleToCrank);
        Transaction tx = new Transaction();
        tx.addInstruction(
                OpenbookProgram.consumeEvents(
                        caller,
                        market.getMarketId(),
                        market.getEventHeap(),
                        peopleToCrank,
                        limit
                )
        );

        String consumeEventsTx;
        try {
            consumeEventsTx = client.getApi().sendTransaction(
                    tx,
                    List.of(caller),
                    null
            );
        } catch (RpcException e) {
            log.error("Error cranking: {}", e.getMessage());
            return Optional.empty();
        }

        log.info("Consumed events in TX: {}", consumeEventsTx);
        return Optional.of(consumeEventsTx);
    }
}
