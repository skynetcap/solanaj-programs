package ch.openserum.serum.manager;

import ch.openserum.serum.model.OrderBook;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class OrderBookCacheManager {

    private final RpcClient client;
    private static final Logger LOGGER = LogManager.getLogger(OrderBookCacheManager.class);

    public OrderBookCacheManager(RpcClient client) {
        this.client = client;
    }

    private final LoadingCache<String, OrderBook> orderbookCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build(
                    new CacheLoader<>() {
                        @Override
                        public OrderBook load(String marketId) throws RpcException {
                            // LOGGER.info("Cache Load Orderbook: " + marketId);
                            return OrderBook.readOrderBook(
                                    Base64.getDecoder().decode(
                                            client.getApi().getAccountInfo(
                                                    PublicKey.valueOf(marketId)
                                            )
                                                    .getValue()
                                                    .getData()
                                                    .get(0)
                                    )
                            );
                        }
                    });

    public OrderBook getOrderBook(PublicKey marketId) {
        return orderbookCache.getUnchecked(marketId.toBase58());
    }

}
