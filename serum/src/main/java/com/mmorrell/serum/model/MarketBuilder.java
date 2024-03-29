package com.mmorrell.serum.model;

import com.mmorrell.serum.manager.OrderBookCacheManager;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.config.Commitment;
import org.p2p.solanaj.rpc.types.config.RpcSendTransactionConfig;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Builds a {@link Market} object, which can have polled data including bid/ask {@link OrderBook}s
 */
public class MarketBuilder {

    private RpcClient client;
    private PublicKey publicKey;
    private boolean retrieveOrderbooks = false;
    private boolean retrieveEventQueue = false;
    private boolean retrieveDecimalsOnly = false;
    private boolean orderBookCacheEnabled = false;
    private long minContextSlot = 0L;
    private boolean built = false;
    private byte[] base64AccountInfo;
    private OrderBookCacheManager orderBookCacheManager;

    private Map<PublicKey, Byte> decimalsCache = new ConcurrentHashMap<>();

    public MarketBuilder setRetrieveOrderBooks(boolean retrieveOrderbooks) {
        this.retrieveOrderbooks = retrieveOrderbooks;
        return this;
    }

    public MarketBuilder setOrderBookCacheEnabled(boolean orderBookCacheEnabled) {
        this.orderBookCacheEnabled = orderBookCacheEnabled;
        this.orderBookCacheManager = orderBookCacheEnabled ? new OrderBookCacheManager(this.client) : null;
        return this;
    }

    public MarketBuilder setClient(RpcClient client) {
        this.client = client;
        return this;
    }

    public MarketBuilder setMinContextSlot(long minContextSlot) {
        if (minContextSlot > this.minContextSlot) {
            this.minContextSlot = minContextSlot;
        }
        return this;
    }

    public boolean isRetrieveOrderbooks() {
        return retrieveOrderbooks;
    }

    public boolean isRetrieveEventQueue() {
        return retrieveEventQueue;
    }

    public boolean isRetrieveDecimalsOnly() {
        return retrieveDecimalsOnly;
    }

    public boolean isOrderBookCacheEnabled() {
        return orderBookCacheEnabled;
    }

    public MarketBuilder setRetrieveEventQueue(boolean retrieveEventQueue) {
        this.retrieveEventQueue = retrieveEventQueue;
        return this;
    }

    public MarketBuilder setRetrieveDecimalsOnly(boolean retrieveDecimalsOnly) {
        this.retrieveDecimalsOnly = retrieveDecimalsOnly;
        return this;
    }

    /**
     * Builds a new {@link Market} object with fresh data. Also called during reload().
     *
     * @return {@link Market} object with live data
     */
    public Market build() {
        // Only lookup account info one time since it never changes (except for fees accrued, not important imo)
        if (!built) {
            base64AccountInfo = retrieveAccountData();
        }

        // Read market
        if (base64AccountInfo == null) {
            throw new RuntimeException("Unable to read account data");
        }

        Market market = Market.readMarket(base64AccountInfo);

        // Get Order books
        if (retrieveOrderbooks) {
            // Data from the token mints
            // first, check the cache for the byte. otherwise, make a request for it
            byte baseDecimals;
            byte quoteDecimals;

            if (decimalsCache.containsKey(market.getBaseMint())) {
                baseDecimals = decimalsCache.get(market.getBaseMint());
            } else {
                baseDecimals = getMintDecimals(market.getBaseMint());
                decimalsCache.put(market.getBaseMint(), baseDecimals);
            }

            if (decimalsCache.containsKey(market.getQuoteMint())) {
                quoteDecimals = decimalsCache.get(market.getQuoteMint());
            } else {
                quoteDecimals = getMintDecimals(market.getQuoteMint());
                decimalsCache.put(market.getQuoteMint(), quoteDecimals);
            }

            market.setBaseDecimals(baseDecimals);
            market.setQuoteDecimals(quoteDecimals);

            // Data from the order books (multithreaded)
            final CompletableFuture<OrderBook> bidThread = CompletableFuture.supplyAsync(() -> retrieveOrderBook(market.getBids()));
            final CompletableFuture<OrderBook> askThread = CompletableFuture.supplyAsync(() -> retrieveOrderBook(market.getAsks()));
            final CompletableFuture<Void> combinedFutures = CompletableFuture.allOf(bidThread, askThread);

            OrderBook bidOrderBook, askOrderBook;
            try {
                combinedFutures.get();
                bidOrderBook = bidThread.get();
                askOrderBook = askThread.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            // TODO - Investigate this hideous pattern
            bidOrderBook.setBaseDecimals(baseDecimals);
            bidOrderBook.setQuoteDecimals(quoteDecimals);
            askOrderBook.setBaseDecimals(baseDecimals);
            askOrderBook.setQuoteDecimals(quoteDecimals);

            bidOrderBook.setBaseLotSize(market.getBaseLotSize());
            bidOrderBook.setQuoteLotSize(market.getQuoteLotSize());
            askOrderBook.setBaseLotSize(market.getBaseLotSize());
            askOrderBook.setQuoteLotSize(market.getQuoteLotSize());

            market.setBidOrderBook(bidOrderBook);
            market.setAskOrderBook(askOrderBook);
        }

        if (retrieveEventQueue) {
            byte[] base64EventQueue = retrieveAccountData(market.getEventQueueKey());

            // first, check the cache for the byte. otherwise, make a request for it
            // TODO - unduplicate this code
            byte baseDecimals;
            byte quoteDecimals;

            if (decimalsCache.containsKey(market.getBaseMint())) {
                baseDecimals = decimalsCache.get(market.getBaseMint());
            } else {
                baseDecimals = getMintDecimals(market.getBaseMint());
                decimalsCache.put(market.getBaseMint(), baseDecimals);
            }

            if (decimalsCache.containsKey(market.getQuoteMint())) {
                quoteDecimals = decimalsCache.get(market.getQuoteMint());
            } else {
                quoteDecimals = getMintDecimals(market.getQuoteMint());
                decimalsCache.put(market.getQuoteMint(), quoteDecimals);
            }

            market.setBaseDecimals(baseDecimals);
            market.setQuoteDecimals(quoteDecimals);

            long baseLotSize = market.getBaseLotSize();
            long quoteLotSize = market.getQuoteLotSize();

            EventQueue eventQueue = EventQueue.readEventQueue(base64EventQueue, baseDecimals, quoteDecimals, baseLotSize, quoteLotSize);
            market.setEventQueue(eventQueue);
        }

        // Used by SerumManager for most lightweight lookup possible
        if (!retrieveEventQueue && !retrieveOrderbooks && retrieveDecimalsOnly) {
            byte baseDecimals;
            byte quoteDecimals;

            if (decimalsCache.containsKey(market.getBaseMint())) {
                baseDecimals = decimalsCache.get(market.getBaseMint());
            } else {
                baseDecimals = getMintDecimals(market.getBaseMint());
                decimalsCache.put(market.getBaseMint(), baseDecimals);
            }

            if (decimalsCache.containsKey(market.getQuoteMint())) {
                quoteDecimals = decimalsCache.get(market.getQuoteMint());
            } else {
                quoteDecimals = getMintDecimals(market.getQuoteMint());
                decimalsCache.put(market.getQuoteMint(), quoteDecimals);
            }

            market.setBaseDecimals(baseDecimals);
            market.setQuoteDecimals(quoteDecimals);
        }

        built = true;
        return market;
    }

    /**
     * Retrieves decimals for a given Token Mint's {@link PublicKey} from Solana account data.
     *
     * @param tokenMint
     * @return
     */
    private byte getMintDecimals(PublicKey tokenMint) {
        if (tokenMint.equals(SerumUtils.WRAPPED_SOL_MINT)) {
            return 9;
        }

        // USDC and USDT cases
        if (tokenMint.equals(SerumUtils.USDC_MINT) || tokenMint.equals(SerumUtils.USDT_MINT)) {
            return 6;
        }

        // 100ms sleep to avoid rate limit
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // RPC call to get mint's account data into decoded bytes (already base64 decoded)
        byte[] accountData = retrieveAccountDataConfirmed(tokenMint);

        // Deserialize accountData into the MINT_LAYOUT enum
        byte decimals = SerumUtils.readDecimalsFromTokenMintData(accountData);

        return decimals;
    }

    private byte[] retrieveAccountData() {
        return retrieveAccountData(publicKey);
    }

    private byte[] retrieveAccountData(PublicKey publicKey) {
        AccountInfo orderBook = null;

        try {
            orderBook = client.getApi().getAccountInfo(
                    publicKey,
                    Map.of(
                            "commitment",
                            Commitment.PROCESSED,
                            "encoding",
                            RpcSendTransactionConfig.Encoding.base64.getEncoding(),
                            "minContextSlot",
                            minContextSlot
                    )
            );
            setMinContextSlot(orderBook.getContext().getSlot());

            final List<String> accountData = orderBook.getValue().getData();
            return Base64.getDecoder().decode(accountData.get(0));
        } catch (RpcException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    private byte[] retrieveAccountDataConfirmed(PublicKey publicKey) {
        AccountInfo orderBook = null;

        try {
            orderBook = client.getApi().getAccountInfo(
                    publicKey,
                    Map.of(
                            "commitment",
                            Commitment.CONFIRMED,
                            "encoding",
                            RpcSendTransactionConfig.Encoding.base64.getEncoding()
                    )
            );

            final List<String> accountData = orderBook.getValue().getData();
            return Base64.getDecoder().decode(accountData.get(0));
        } catch (RpcException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public MarketBuilder setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public Market reload() {
        return build();
    }

    private OrderBook retrieveOrderBook(PublicKey publicKey) {
        if (orderBookCacheEnabled) {
            // Use a 1-second expireAfterWrite cache if enabled.
            return orderBookCacheManager.getOrderBook(publicKey);
        } else {
            // Fresh hit
            try {
                return OrderBook.readOrderBook(
                        Base64.getDecoder().decode(
                                client.getApi().getAccountInfo(
                                                publicKey,
                                                Map.of("commitment", Commitment.PROCESSED)
                                        )
                                        .getValue()
                                        .getData()
                                        .get(0)
                        )
                );
            } catch (RpcException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
