package com.mmorrell.openbook.manager;

import com.google.common.io.Files;
import com.mmorrell.openbook.OpenBookUtil;
import com.mmorrell.openbook.model.BookSide;
import com.mmorrell.openbook.model.OpenBookEventHeap;
import com.mmorrell.openbook.model.OpenBookMarket;
import com.mmorrell.openbook.model.OpenBookOpenOrdersAccount;
import com.mmorrell.openbook.program.OpenbookProgram;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.ComputeBudgetProgram;
import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.ProgramAccount;
import org.p2p.solanaj.rpc.types.config.Commitment;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The OpenBookManager class is responsible for managing OpenBook markets and their related data.
 */
@Slf4j
public class OpenBookManager {

    private final RpcClient client;
    private final Map<PublicKey, OpenBookMarket> marketCache = new HashMap<>();

    private final static int CONSUME_EVENTS_DEFAULT_FEE = 11;
    private final static int DEFAULT_PRIORITY_LIMIT = 50_000;

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
            log.error("Error caching OpenBook v2 markets: {}", e.getMessage(), e);
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
                // Use cache here anyway. The GPA will pick up all markets
                OpenBookMarket openBookMarket = marketCache.get(marketId);

                if (openBookMarket == null) {
                    return Optional.empty();
                }

                if (retrieveOrderBooks) {
                    Map<PublicKey, Optional<AccountInfo.Value>> books = client.getApi().getMultipleAccountsMap(
                            List.of(openBookMarket.getBids(), openBookMarket.getAsks())
                    );

                    Optional<AccountInfo.Value> bidOrderBookValue = books.get(openBookMarket.getBids());
                    Optional<AccountInfo.Value> askOrderBookValue = books.get(openBookMarket.getAsks());

                    if (bidOrderBookValue.isPresent() && askOrderBookValue.isPresent()) {
                        byte[] bidData =
                                Base64.getDecoder().decode(bidOrderBookValue.get().getData().get(0).getBytes());
                        BookSide bids = BookSide.readBookSide(bidData);
                        bids.setBaseDecimals(openBookMarket.getBaseDecimals());
                        bids.setQuoteDecimals(openBookMarket.getQuoteDecimals());
                        bids.setBaseLotSize(openBookMarket.getBaseLotSize());
                        bids.setQuoteLotSize(openBookMarket.getQuoteLotSize());
                        openBookMarket.setBidOrders(bids.getOrders());

                        byte[] askData =
                                Base64.getDecoder().decode(askOrderBookValue.get().getData().get(0).getBytes());
                        BookSide asks = BookSide.readBookSide(askData);
                        asks.setBaseDecimals(openBookMarket.getBaseDecimals());
                        asks.setQuoteDecimals(openBookMarket.getQuoteDecimals());
                        asks.setBaseLotSize(openBookMarket.getBaseLotSize());
                        asks.setQuoteLotSize(openBookMarket.getQuoteLotSize());
                        openBookMarket.setAskOrders(asks.getOrders());
                    }
                }

                return Optional.of(openBookMarket);
            } catch (Exception e) {
                log.error("Unable to retrieve OpenBook v2 market {}", marketId, e);
                return Optional.empty();
            }
        }
    }

    /**
     * Retrieves the OpenBookEventHeap associated with the given eventHeap Public Key.
     *
     * @param eventHeap The Public Key of the event heap to retrieve.
     * @return An Optional containing the OpenBookEventHeap if retrieval is successful,
     * otherwise an empty Optional.
     */
    public Optional<OpenBookEventHeap> getEventHeap(PublicKey eventHeap) {
        try {
            OpenBookEventHeap openBookEventHeap = OpenBookEventHeap.readOpenBookEventHeap(
                    client.getApi()
                            .getAccountInfo(eventHeap, Map.of("commitment", Commitment.PROCESSED))
                            .getDecodedData()
            );

            return Optional.of(openBookEventHeap);
        } catch (Exception e) {
            log.error("Error getting event heap: {}", e.getMessage(), e);
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
        } catch (Exception e) {
            log.error("Error getting OOA: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Consume events in an OpenBook market.
     *
     * @param caller   The account making the request.
     * @param marketId The public key of the market to consume events from.
     * @param limit    The maximum number of events to consume.
     * @return An Optional containing the transaction hash if events are consumed successfully,
     * otherwise an empty Optional.
     */
    public Optional<String> consumeEvents(Account caller, PublicKey marketId, long limit, @Nullable String memo,
                                          int priorityFee, int priorityLimit) {
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

        Set<PublicKey> peopleToCrank = new HashSet<>(eventHeap.getEventOwnersToConsume());
        List<PublicKey> openOrdersAccounts = peopleToCrank.stream().toList()
                .subList(0, Math.min((int) limit, peopleToCrank.size()));

        log.info("Cranking {}: {}", market.getName(), peopleToCrank);
        Transaction tx = new Transaction();
        tx.addInstruction(ComputeBudgetProgram.setComputeUnitLimit(priorityLimit));
        tx.addInstruction(ComputeBudgetProgram.setComputeUnitPrice(priorityFee));
        tx.addInstruction(
                OpenbookProgram.consumeEvents(
                        caller,
                        market.getMarketId(),
                        market.getEventHeap(),
                        openOrdersAccounts,
                        limit
                )
        );

        if (memo != null) {
            tx.addInstruction(MemoProgram.writeUtf8(caller.getPublicKey(), memo));
        }

        String consumeEventsTx;
        try {
            consumeEventsTx = client.getApi().sendTransaction(
                    tx,
                    List.of(caller),
                    null
            );
        } catch (RpcException e) {
            log.error("Error cranking: {}", e.getMessage(), e);
            return Optional.empty();
        }

        log.info("Consumed events in TX: {}", consumeEventsTx);
        return Optional.of(consumeEventsTx);
    }


    /**
     * Consumes events for a given account, market ID, limit, and optional memo.
     * It uses the default fee.
     *
     * @param caller The account performing the consumption
     * @param marketId The public key of the market
     * @param limit The maximum number of events to consume
     * @param memo The optional memo
     * @return An Optional String representing the consumed events
     */
    public Optional<String> consumeEvents(Account caller, PublicKey marketId, long limit, @Nullable String memo) {
        return consumeEvents(caller, marketId, limit, memo, CONSUME_EVENTS_DEFAULT_FEE, DEFAULT_PRIORITY_LIMIT);
    }

    public Optional<String> consumeEvents(Account caller, PublicKey marketId, long limit, @Nullable String memo,
                                          int priorityFee) {
        return consumeEvents(caller, marketId, limit, memo, CONSUME_EVENTS_DEFAULT_FEE, DEFAULT_PRIORITY_LIMIT);
    }
}
