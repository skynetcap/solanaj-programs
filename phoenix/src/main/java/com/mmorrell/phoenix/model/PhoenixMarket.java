package com.mmorrell.phoenix.model;

import com.mmorrell.phoenix.util.PhoenixUtil;
import kotlin.Pair;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    private List<Pair<FIFOOrderId, FIFORestingOrder>> askList;
    private List<Pair<FIFOOrderId, FIFORestingOrder>> askListSanitized;

    private Map<PublicKey, PhoenixTraderState> traders;

    public static PhoenixMarket readPhoenixMarket(byte[] data, PhoenixMarketHeader header) {
        PhoenixMarket phoenixMarket = PhoenixMarket.builder()
                .baseLotsPerBaseUnit(Utils.readInt64(data, START_OFFSET))
                .tickSizeInQuoteLotsPerBaseUnit(Utils.readInt64(data, START_OFFSET + 8))
                .orderSequenceNumber(Utils.readInt64(data, START_OFFSET + 16))
                .takerFeeBps(Utils.readInt64(data, START_OFFSET + 24))
                .collectedQuoteLotFees(Utils.readInt64(data, START_OFFSET + 32))
                .unclaimedQuoteLotFees(Utils.readInt64(data, START_OFFSET + 40))
                .bidList(new ArrayList<>())
                .bidListSanitized(new ArrayList<>())
                .askList(new ArrayList<>())
                .askListSanitized(new ArrayList<>())
                .traders(new HashMap<>())
                .build();

        long bidsSize =
                16 + 16 + (16 + FIFOOrderId.FIFO_ORDER_ID_SIZE + FIFORestingOrder.FIFO_RESTING_ORDER_SIZE) * header.getBidsSize();
        byte[] bidBuffer = Arrays.copyOfRange(data, 880, (int) bidsSize);

        var asksSize =
                16 + 16 + (16 + FIFOOrderId.FIFO_ORDER_ID_SIZE + FIFORestingOrder.FIFO_RESTING_ORDER_SIZE) * header.getAsksSize();
        byte[] askBuffer = Arrays.copyOfRange(data, 880 + (int) bidsSize, 880 + (int) bidsSize + (int) asksSize);

        var tradersSize = 16 + 16 + (16 + 32 + PhoenixTraderState.PHOENIX_TRADER_STATE_SIZE) * header.getNumSeats();
        byte[] traderBuffer = Arrays.copyOfRange(data, 880 + (int) bidsSize + (int) asksSize,
                880 + (int) bidsSize + (int) asksSize + (int) tradersSize);

        readBidBuffer(bidBuffer, phoenixMarket);
        readAskBuffer(askBuffer, phoenixMarket);
        readTraderBuffer(traderBuffer, phoenixMarket);

        return phoenixMarket;
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

            market.getTraders().put(traderPubkey, phoenixTraderState);
            freeListPointersList.add(new Pair<>(index, registers.get(0)));
        }

        Set<Integer> freeNodes = new HashSet<>();
        int indexToRemove = freeListHead - 1;
        int counter = 0;

        while (freeListHead != 0) {
            var next = freeListPointersList.get(freeListHead - 1);
            indexToRemove = next.component1();
            freeListHead = next.component2();

            freeNodes.add(indexToRemove);
            counter += 1;

            if (counter > bumpIndex) {
                log.error("Infinite Loop Detected");
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
            var next = freeListPointersList.get(freeListHead - 1);
            indexToRemove = next.component1();
            freeListHead = next.component2();

            freeNodes.add(indexToRemove);
            counter += 1;

            if (counter > bumpIndex) {
                log.error("Infinite Loop Detected");
            }
        }

        var bidOrdersList = market.getBidList();
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
            var next = freeListPointersList.get(freeListHead - 1);
            indexToRemove = next.component1();
            freeListHead = next.component2();

            freeNodes.add(indexToRemove);
            counter += 1;

            if (counter > bumpIndex) {
                log.error("Infinite Loop Detected");
            }
        }

        var askOrdersList = market.askList;
        for (int i = 0; i < askOrdersList.size(); i++) {
            Pair<FIFOOrderId, FIFORestingOrder> entry = askOrdersList.get(i);
            if (!freeNodes.contains(i)) {
                // tree.set kv
                market.askListSanitized.add(entry);
            }
        }
    }
}
