import com.mmorrell.serum.model.*;
import com.mmorrell.serum.model.Order;
import org.bitcoinj.core.Utils;
import org.junit.jupiter.api.*;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.utils.ByteUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MarketTest {

    private final RpcClient client = new RpcClient("https://mainnet.helius-rpc.com/?api-key=a778b653-bdd6-41bc-8cda-0c7377faf1dd");
    private static final Logger LOGGER = LogManager.getLogger(MarketTest.class);

    /**
     * Sets up the test environment, adding a delay before each test.
     * 
     * @throws InterruptedException if the thread is interrupted during sleep
     */
    @BeforeEach
    public void setUp() throws InterruptedException {
        Thread.sleep(1000); // 1 second delay
    }

    /**
     * Uses a {@link MarketBuilder} class to retrieve data about the BTC/USDC Serum market.
     */
    @Test
    public void marketBuilderBtcUsdcTest() throws RpcException, InterruptedException {
        // Pubkey of SRM/USDC market
        final PublicKey publicKey = new PublicKey("8BnEgHoWFysVcuFFX7QztDmzuH8r5ZFvyP3sYwn1XTh6"); //SRM/USDC

        final Market solUsdcMarket = new MarketBuilder()
                .setClient(client)
                .setPublicKey(publicKey)
                .setRetrieveOrderBooks(true)
                .build();

        final OrderBook bids = solUsdcMarket.getBidOrderBook();
        final OrderBook asks = solUsdcMarket.getAskOrderBook();

        LOGGER.info("Best bid = " + bids.getBestBid().getPrice() / 1000.0);
        LOGGER.info("Best ask = " + asks.getBestAsk().getPrice() / 1000.0);

        // Verify at least 1 bid and 1 ask (should always be for BTC/USDC)
        assertTrue(bids.getOrders().size() > 0);
        assertTrue(asks.getOrders().size() > 0);

        AccountInfo mktInfo = client.getApi().getAccountInfo(solUsdcMarket.getEventQueueKey());

        LOGGER.info("mktInfo [len]: " + mktInfo.toString().length());
        LOGGER.info("Slot: " + mktInfo.getContext().getSlot());

        Thread.sleep(2000L);

        LOGGER.info("---- Using minContextSlot ----");

        AccountInfo mktInfoWithSlot = client.getApi().getAccountInfo(
                solUsdcMarket.getEventQueueKey(),
                Map.of(
                    "minContextSlot", mktInfo.getContext().getSlot()
                )
        );

        LOGGER.info("mktInfo [len]: " + mktInfo.toString().length());
        LOGGER.info("Slot: " + mktInfo.getContext().getSlot());
    }

    @Test
    @Disabled
    public void orderBookCacheTest() throws InterruptedException {
        final PublicKey marketId = new PublicKey("8BnEgHoWFysVcuFFX7QztDmzuH8r5ZFvyP3sYwn1XTh6");
        final MarketBuilder solUsdcMarketBuilder = new MarketBuilder()
                .setClient(client)
                .setPublicKey(marketId)
                .setRetrieveOrderBooks(true)
                .setOrderBookCacheEnabled(true);

        Market solUsdcMarket = solUsdcMarketBuilder.build();
        Order bestBid = solUsdcMarket.getBidOrderBook().getBestBid();
        Order bestAsk = solUsdcMarket.getAskOrderBook().getBestAsk();

        LOGGER.debug(
                String.format(
                        "%.2f x %.2f, %.2f x %.2f",
                        bestBid.getFloatPrice(),
                        bestBid.getFloatQuantity(),
                        bestAsk.getFloatPrice(),
                        bestAsk.getFloatQuantity()
                )
        );

        for (int i = 0; i < 500; i++) {
            solUsdcMarket = solUsdcMarketBuilder.reload();
            bestBid = solUsdcMarket.getBidOrderBook().getBestBid();
            bestAsk = solUsdcMarket.getAskOrderBook().getBestAsk();

            LOGGER.debug(
                    String.format(
                            "%.2f x %.2f, %.2f x %.2f",
                            bestBid.getFloatQuantity(),
                            bestBid.getFloatPrice(),
                            bestAsk.getFloatQuantity(),
                            bestAsk.getFloatPrice()
                    )
            );

            Thread.sleep(200L);
        }
    }

    /**
     * Verifies that {@link OrderBook} headers are properly read by {@link OrderBook#readOrderBook(byte[])}
     */
    @Test
    @Disabled
    public void orderBookTest() {
        byte[] data = new byte[0];

        try {
            data = Files.readAllBytes(Paths.get("src/test/resources/orderbook.bin"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        OrderBook bidOrderBook = OrderBook.readOrderBook(data);

        LOGGER.info(bidOrderBook.getAccountFlags().toString());

        Slab slab = bidOrderBook.getSlab();

        assertNotNull(slab);
        assertEquals(141, slab.getBumpIndex());
        assertEquals(78, slab.getFreeListLen());
        assertEquals(56, slab.getFreeListHead());
        assertEquals(32, slab.getLeafCount());
    }

    /**
     * Will verify {@link ByteUtils} or {@link SerumUtils} can read seqNum and price.
     * Currently just reads price and logs it.
     */
    @Test
    public void testPriceDeserialization() {
        /* C:\apps\solanaj\lqidusdc.bin (1/12/2021 8:55:59 AM)
   StartOffset(d): 00001277, EndOffset(d): 00001292, Length(d): 00000016 */

        byte[] rawData = {
                (byte) 0xDB, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        };

        long seqNum = Utils.readInt64(rawData, 0);
        long price = Utils.readInt64(rawData, 8);

        LOGGER.info("Price = " + price);
        LOGGER.info("seqNum = " + seqNum);

        assertEquals(1, price);
        assertEquals(seqNum, -293L);
    }

    /**
     * Uses a {@link MarketBuilder} class to retrieve data about the SOL/USDC Serum market.
     */
    @Test
    public void marketBuilderSolUsdcTest() {
        final PublicKey solUsdcPublicKey = new PublicKey("8BnEgHoWFysVcuFFX7QztDmzuH8r5ZFvyP3sYwn1XTh6");

        final Market solUsdcMarket = new MarketBuilder()
                .setClient(client)
                .setPublicKey(solUsdcPublicKey)
                .setRetrieveOrderBooks(true)
                .build();

        final OrderBook bids = solUsdcMarket.getBidOrderBook();
        final OrderBook asks = solUsdcMarket.getAskOrderBook();
        LOGGER.info("Market = " + solUsdcMarket.toString());

        final ArrayList<Order> asksOrders = asks.getOrders();
        asksOrders.sort(Comparator.comparingLong(Order::getPrice).reversed());
        asksOrders.forEach(order -> {
            System.out.printf("SOL/USDC Ask: $%.4f (Quantity: %.4f)%n", order.getFloatPrice(), order.getFloatQuantity());
        });

        LOGGER.info("Bids");

        final ArrayList<Order> orders = bids.getOrders();
        orders.sort(Comparator.comparingLong(Order::getPrice).reversed());
        orders.forEach(order -> {
            System.out.printf("SOL/USDC Bid: $%.4f (Quantity: %.4f)%n", order.getFloatPrice(), order.getFloatQuantity());
        });

        // Verify that an order exists
        assertTrue(orders.size() > 0);
    }

    /**
     * Uses a {@link MarketBuilder} class to retrieve data about the SOL/USDC Serum market.
     */
    @Test
    public void marketBuilderwhEthUsdcTest() {
        final PublicKey solUsdcPublicKey = new PublicKey("8BnEgHoWFysVcuFFX7QztDmzuH8r5ZFvyP3sYwn1XTh6");

        final Market solUsdcMarket = new MarketBuilder()
                .setClient(client)
                .setPublicKey(solUsdcPublicKey)
                .setRetrieveOrderBooks(true)
                .build();

        final OrderBook bids = solUsdcMarket.getBidOrderBook();
        final OrderBook asks = solUsdcMarket.getAskOrderBook();
        LOGGER.info("Market = " + solUsdcMarket.toString());

        final ArrayList<Order> asksOrders = asks.getOrders();
        asksOrders.sort(Comparator.comparingLong(Order::getPrice).reversed());
        asksOrders.forEach(order -> {
            System.out.printf("whETH/USDC Ask: $%.4f (Quantity: %.4f)%n", order.getFloatPrice(), order.getFloatQuantity());
        });

        LOGGER.info("Bids");

        final ArrayList<Order> orders = bids.getOrders();
        orders.sort(Comparator.comparingLong(Order::getPrice).reversed());
        orders.forEach(order -> {
            System.out.printf("whETH/USDC Bid: $%.4f (Quantity: %.4f)%n", order.getFloatPrice(), order.getFloatQuantity());
        });

        // Verify that an order exists
        assertTrue(orders.size() > 0);
    }

    /**
     * Uses a {@link MarketBuilder} class to retrieve the Event Queue from the SOL/USDC Serum market.
     */
    @Test
    @Disabled
    public void marketBuilderEventQueueTest() {
        final PublicKey solUsdcPublicKey = new PublicKey("8BnEgHoWFysVcuFFX7QztDmzuH8r5ZFvyP3sYwn1XTh6");

        final MarketBuilder solUsdcMarketBuilder = new MarketBuilder()
                .setPublicKey(solUsdcPublicKey)
                .setClient(client)
                .setRetrieveOrderBooks(true)
                .setRetrieveEventQueue(true);

        Market solUsdcMarket = solUsdcMarketBuilder.build();

        LOGGER.info("Market = " + solUsdcMarket.toString());
        LOGGER.info("Event Queue = " + solUsdcMarket.getEventQueue());

        List<TradeEvent> tradeEvents = solUsdcMarket.getEventQueue().getEvents();

        tradeEvents.forEach(tradeEvent -> {
            LOGGER.info(tradeEvent.toString());
        });

    }

}
