package com.mmorrell.phoenix.model;

import com.mmorrell.phoenix.util.PhoenixUtil;
import kotlin.Pair;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Data
@Builder
@Slf4j
public class PhoenixMarket {

    // B trees start at offset 880
    private static final int START_OFFSET = 832;
    private long baseLotsPerBaseUnit;
    private long tickSizeInQuoteLotsPerBaseUnit;
    private long orderSequenceNumber;
    private long takerFeeBps;
    private long collectedQuoteLotFees;
    private long unclaimedQuoteLotFees;

    private List<Pair<FIFOOrderId, FIFORestingOrder>> bidList;
    private List<Pair<FIFOOrderId, FIFORestingOrder>> bidListSanitized;
    private List<PhoenixOrder> bidListNormalized;

    private List<Pair<FIFOOrderId, FIFORestingOrder>> askList;
    private List<Pair<FIFOOrderId, FIFORestingOrder>> askListSanitized;
    private List<PhoenixOrder> askListNormalized;

    private List<Pair<PublicKey, PhoenixTraderState>> traders;
    private List<Pair<PublicKey, PhoenixTraderState>> tradersSanitized;
    private Map<PublicKey, PhoenixTraderState> tradersNormalized;

    private PhoenixMarketHeader phoenixMarketHeader;
    private PublicKey marketId;

    public Optional<Pair<FIFOOrderId, FIFORestingOrder>> getBestBid() {
        if (bidListSanitized.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(bidListSanitized.stream()
                .sorted((o1, o2) -> Math.toIntExact(o2.getFirst().getPriceInTicks() - o1.getFirst().getPriceInTicks()))
                .toList()
                .get(0));
    }

    public Optional<Pair<FIFOOrderId, FIFORestingOrder>> getBestAsk() {
        if (askListSanitized.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(askListSanitized.stream()
                .sorted((o1, o2) -> Math.toIntExact(o1.getFirst().getPriceInTicks() - o2.getFirst().getPriceInTicks()))
                .toList()
                .get(0));
    }

    public static PhoenixMarket readPhoenixMarket(byte[] data) {
        PhoenixMarket phoenixMarket = PhoenixMarket.builder()
                .baseLotsPerBaseUnit(Utils.readInt64(data, START_OFFSET))
                .tickSizeInQuoteLotsPerBaseUnit(Utils.readInt64(data, START_OFFSET + 8))
                .orderSequenceNumber(Utils.readInt64(data, START_OFFSET + 16))
                .takerFeeBps(Utils.readInt64(data, START_OFFSET + 24))
                .collectedQuoteLotFees(Utils.readInt64(data, START_OFFSET + 32))
                .unclaimedQuoteLotFees(Utils.readInt64(data, START_OFFSET + 40))
                .bidList(new ArrayList<>())
                .bidListSanitized(new ArrayList<>())
                .bidListNormalized(new ArrayList<>())
                .askList(new ArrayList<>())
                .askListSanitized(new ArrayList<>())
                .askListNormalized(new ArrayList<>())
                .traders(new ArrayList<>())
                .tradersSanitized(new ArrayList<>())
                .tradersNormalized(new HashMap<>())
                .phoenixMarketHeader(PhoenixMarketHeader.readPhoenixMarketHeader(data))
                .build();

        long bidsSize =
                16 + 16 + (16 + FIFOOrderId.FIFO_ORDER_ID_SIZE + FIFORestingOrder.FIFO_RESTING_ORDER_SIZE) * phoenixMarket.getPhoenixMarketHeader().getBidsSize();
        byte[] bidBuffer = Arrays.copyOfRange(data, 880, (int) bidsSize);

        long asksSize =
                16 + 16 + (16 + FIFOOrderId.FIFO_ORDER_ID_SIZE + FIFORestingOrder.FIFO_RESTING_ORDER_SIZE) * phoenixMarket.getPhoenixMarketHeader().getAsksSize();
        byte[] askBuffer = Arrays.copyOfRange(data, 880 + (int) bidsSize, 880 + (int) bidsSize + (int) asksSize);

        long tradersSize =
                16 + 16 + (16 + 32 + PhoenixTraderState.PHOENIX_TRADER_STATE_SIZE) * phoenixMarket.getPhoenixMarketHeader().getNumSeats();
        byte[] traderBuffer = Arrays.copyOfRange(data, 880 + (int) bidsSize + (int) asksSize,
                880 + (int) bidsSize + (int) asksSize + (int) tradersSize);

        readBidBuffer(bidBuffer, phoenixMarket);
        readAskBuffer(askBuffer, phoenixMarket);
        readTraderBuffer(traderBuffer, phoenixMarket);
        normalizeOrders(phoenixMarket);
        normalizeTraders(phoenixMarket);

        return phoenixMarket;
    }

    private static void normalizeTraders(PhoenixMarket market) {
        Map<PublicKey, PhoenixTraderState> stateMap = market.getTradersNormalized();
        market.getTradersSanitized().forEach(traderStatePair -> {
            stateMap.put(traderStatePair.getFirst(), traderStatePair.getSecond());
        });
    }

    private static void normalizeOrders(PhoenixMarket market) {
        market.setBidListNormalized(new ArrayList<>());
        market.getBidListSanitized().forEach(order -> {
            // Ensure the trader index is valid
            int traderIndex = (int) (order.getSecond().getTraderIndex() - 1);
            if (traderIndex >= 0 && traderIndex < market.getTraders().size()) {
                double price = calculatePrice(order, market);
                double size = calculateSize(order, market);

                market.getBidListNormalized().add(
                        PhoenixOrder.builder()
                                .price(price)
                                .size(size)
                                .trader(market.getTraders().get(traderIndex).getFirst())
                                .build()
                );
            } else {
                log.error("Invalid trader index for bid order: " + traderIndex);
            }
        });
        market.getBidListNormalized().sort(Comparator.comparingDouble(PhoenixOrder::getPrice));

        market.setAskListNormalized(new ArrayList<>());
        market.getAskListSanitized().forEach(order -> {
            // Ensure the trader index is valid
            int traderIndex = (int) (order.getSecond().getTraderIndex() - 1);
            if (traderIndex >= 0 && traderIndex < market.getTraders().size()) {
                double price = calculatePrice(order, market);
                double size = calculateSize(order, market);

                market.getAskListNormalized().add(
                        PhoenixOrder.builder()
                                .price(price)
                                .size(size)
                                .trader(market.getTraders().get(traderIndex).getFirst())
                                .build()
                );
            } else {
                log.error("Invalid trader index for ask order: " + traderIndex);
            }
        });
        market.getAskListNormalized().sort(Comparator.comparingDouble(PhoenixOrder::getPrice));
    }

    private static double calculatePrice(Pair<FIFOOrderId, FIFORestingOrder> order, PhoenixMarket market) {
        return ((double) order.getFirst().getPriceInTicks() *
                market.getTickSizeInQuoteLotsPerBaseUnit() *
                market.getPhoenixMarketHeader().getQuoteLotSize()) /
                (Math.pow(10, market.getPhoenixMarketHeader().getQuoteDecimals()) *
                        market.getPhoenixMarketHeader().getRawBaseUnitsPerBaseUnit());
    }

    private static double calculateSize(Pair<FIFOOrderId, FIFORestingOrder> order, PhoenixMarket market) {
        return ((double) order.getSecond().getNumBaseLots() * market.getPhoenixMarketHeader().getBaseLotSize()) /
                Math.pow(10, market.getPhoenixMarketHeader().getBaseDecimals());
    }

    private static void readTraderBuffer(byte[] traderBuffer, PhoenixMarket market) {
        int offset = 0;
        offset += 16; // skip rbtree header
        offset += 8;  // Skip node allocator size

        int bumpIndex = PhoenixUtil.readInt32(traderBuffer, offset);
        offset += 4;

        int freeListHead = PhoenixUtil.readInt32(traderBuffer, offset);
        offset += 4;

        List<Pair<Integer, Integer>> freeListPointersList = new ArrayList<>();

        for (int index = 0; offset < traderBuffer.length && index < bumpIndex; index++) {
            List<Integer> registers = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                registers.add(PhoenixUtil.readInt32(traderBuffer, offset));
                offset += 4;
            }

            PublicKey traderPubkey = PublicKey.readPubkey(traderBuffer, offset);
            offset += 32;

            PhoenixTraderState phoenixTraderState = PhoenixTraderState.readPhoenixTraderState(
                    Arrays.copyOfRange(traderBuffer, offset, offset + PhoenixTraderState.PHOENIX_TRADER_STATE_SIZE)
            );
            offset += PhoenixTraderState.PHOENIX_TRADER_STATE_SIZE;

            market.getTraders().add(new Pair<>(traderPubkey, phoenixTraderState));
            freeListPointersList.add(new Pair<>(index, registers.get(0)));
        }

        Set<Integer> freeNodes = new HashSet<>();
        int indexToRemove = freeListHead - 1;
        int counter = 0;

        while (freeListHead != 0) {
            // Ensure freeListHead is within bounds
            if (freeListHead - 1 < freeListPointersList.size()) {
                Pair<Integer, Integer> next = freeListPointersList.get(freeListHead - 1);
                indexToRemove = next.component1();
                freeListHead = next.component2();

                freeNodes.add(indexToRemove);
                counter += 1;

                if (counter > bumpIndex) {
                    log.error("Infinite Loop Detected");
                }
            } else {
                log.error("freeListHead out of bounds: " + freeListHead);
                break; // Exit the loop if out of bounds
            }
        }

        List<Pair<PublicKey, PhoenixTraderState>> traderList = market.getTraders();
        for (int i = 0; i < traderList.size(); i++) {
            Pair<PublicKey, PhoenixTraderState> entry = traderList.get(i);
            if (!freeNodes.contains(i)) {
                // tree.set kv
                market.tradersSanitized.add(entry);
            }
        }
    }

    private static void readBidBuffer(byte[] bidBuffer, PhoenixMarket market) {
        int offset = 0;
        offset += 16; // skip rbtree header
        offset += 8;  // Skip node allocator size

        int bumpIndex = PhoenixUtil.readInt32(bidBuffer, offset);
        offset += 4;

        int freeListHead = PhoenixUtil.readInt32(bidBuffer, offset);
        offset += 4;

        List<Pair<Integer, Integer>> freeListPointersList = new ArrayList<>();

        for (int index = 0; offset < bidBuffer.length && index < bumpIndex; index++) {
            List<Integer> registers = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                registers.add(PhoenixUtil.readInt32(bidBuffer, offset));
                offset += 4;
            }

            FIFOOrderId fifoOrderId = FIFOOrderId.readFifoOrderId(
                    Arrays.copyOfRange(bidBuffer, offset, offset + 16)
            );
            offset += FIFOOrderId.FIFO_ORDER_ID_SIZE;

            FIFORestingOrder fifoRestingOrder = FIFORestingOrder.readFifoRestingOrder(
                    Arrays.copyOfRange(bidBuffer, offset, offset + 32)
            );
            offset += FIFORestingOrder.FIFO_RESTING_ORDER_SIZE;

            market.getBidList().add(new Pair<>(fifoOrderId, fifoRestingOrder));
            freeListPointersList.add(new Pair<>(index, registers.get(0)));
        }

        Set<Integer> freeNodes = new HashSet<>();
        int indexToRemove = freeListHead - 1;
        int counter = 0;

        while (freeListHead != 0) {
            // Ensure freeListHead is within bounds
            if (freeListHead - 1 < freeListPointersList.size()) {
                Pair<Integer, Integer> next = freeListPointersList.get(freeListHead - 1);
                indexToRemove = next.component1();
                freeListHead = next.component2();

                freeNodes.add(indexToRemove);
                counter += 1;

                if (counter > bumpIndex) {
                    log.error("Infinite Loop Detected");
                }
            } else {
                log.error("freeListHead out of bounds: " + freeListHead);
                break; // Exit the loop if out of bounds
            }
        }

        List<Pair<FIFOOrderId, FIFORestingOrder>> bidOrdersList = market.getBidList();
        for (int i = 0; i < bidOrdersList.size(); i++) {
            Pair<FIFOOrderId, FIFORestingOrder> entry = bidOrdersList.get(i);
            if (!freeNodes.contains(i)) {
                // tree.set kv
                market.bidListSanitized.add(entry);
            }
        }
    }

    private static void readAskBuffer(byte[] bidBuffer, PhoenixMarket market) {
        int offset = 0;
        offset += 16; // skip rbtree header
        offset += 8;  // Skip node allocator size

        int bumpIndex = PhoenixUtil.readInt32(bidBuffer, offset);
        offset += 4;

        int freeListHead = PhoenixUtil.readInt32(bidBuffer, offset);
        offset += 4;

        List<Pair<Integer, Integer>> freeListPointersList = new ArrayList<>();

        for (int index = 0; offset < bidBuffer.length && index < bumpIndex; index++) {
            List<Integer> registers = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                registers.add(PhoenixUtil.readInt32(bidBuffer, offset));
                offset += 4;
            }

            FIFOOrderId fifoOrderId = FIFOOrderId.readFifoOrderId(
                    Arrays.copyOfRange(bidBuffer, offset, offset + 16)
            );
            offset += FIFOOrderId.FIFO_ORDER_ID_SIZE;

            FIFORestingOrder fifoRestingOrder = FIFORestingOrder.readFifoRestingOrder(
                    Arrays.copyOfRange(bidBuffer, offset, offset + 32)
            );
            offset += FIFORestingOrder.FIFO_RESTING_ORDER_SIZE;

            market.askList.add(new Pair<>(fifoOrderId, fifoRestingOrder));
            freeListPointersList.add(new Pair<>(index, registers.get(0)));
        }

        Set<Integer> freeNodes = new HashSet<>();
        int indexToRemove = freeListHead - 1;
        int counter = 0;

        while (freeListHead != 0) {
            // Ensure freeListHead is within bounds
            if (freeListHead - 1 < freeListPointersList.size()) {
                Pair<Integer, Integer> next = freeListPointersList.get(freeListHead - 1);
                indexToRemove = next.component1();
                freeListHead = next.component2();

                freeNodes.add(indexToRemove);
                counter += 1;

                if (counter > bumpIndex) {
                    log.error("Infinite Loop Detected");
                }
            } else {
                log.error("freeListHead out of bounds: " + freeListHead);
                break; // Exit the loop if out of bounds
            }
        }

        List<Pair<FIFOOrderId, FIFORestingOrder>> askOrdersList = market.getAskList();
        for (int i = 0; i < askOrdersList.size(); i++) {
            Pair<FIFOOrderId, FIFORestingOrder> entry = askOrdersList.get(i);
            if (!freeNodes.contains(i)) {
                // tree.set kv
                market.askListSanitized.add(entry);
            }
        }
    }

    public long convertPriceToPriceInTicks(double price) {
        // Multiply by denominator
        double result = price * (Math.pow(10, phoenixMarketHeader.getQuoteDecimals()) *
                phoenixMarketHeader.getRawBaseUnitsPerBaseUnit());

        // Divide out getTickSizeInQuoteLotsPerBaseUnit and getQuoteLotSize
        result = result / tickSizeInQuoteLotsPerBaseUnit;
        result = result / phoenixMarketHeader.getQuoteLotSize();

        return Math.round(result);
    }

    public long convertSizeToNumBaseLots(double size) {
        double result = size * Math.pow(10, phoenixMarketHeader.getBaseDecimals());
        result = result / phoenixMarketHeader.getBaseLotSize();

        return Math.round(result);
    }

    public long convertSizeToNumQuoteLots(double size) {
        return Math.round(size * Math.pow(10, phoenixMarketHeader.getQuoteDecimals()) /
                phoenixMarketHeader.getQuoteLotSize());
    }
}
