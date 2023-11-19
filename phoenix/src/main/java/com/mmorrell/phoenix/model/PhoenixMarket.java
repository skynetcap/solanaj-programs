package com.mmorrell.phoenix.model;

import com.mmorrell.phoenix.util.PhoenixUtil;
import kotlin.Pair;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

    @Getter
    public static List<Pair<FIFOOrderId, FIFORestingOrder>> bidList;

    @Getter
    public static List<Pair<FIFOOrderId, FIFORestingOrder>> bidListSanitized;

    public static PhoenixMarket readPhoenixMarket(byte[] data, PhoenixMarketHeader header) {
        PhoenixMarket phoenixMarket = PhoenixMarket.builder()
                .baseLotsPerBaseUnit(Utils.readInt64(data, START_OFFSET))
                .tickSizeInQuoteLotsPerBaseUnit(Utils.readInt64(data, START_OFFSET + 8))
                .orderSequenceNumber(Utils.readInt64(data, START_OFFSET + 16))
                .takerFeeBps(Utils.readInt64(data, START_OFFSET + 24))
                .collectedQuoteLotFees(Utils.readInt64(data, START_OFFSET + 32))
                .unclaimedQuoteLotFees(Utils.readInt64(data, START_OFFSET + 40))
                .build();

        long bidsSize =
                16 + 16 + (16 + FIFOOrderId.FIFO_ORDER_ID_SIZE + FIFORestingOrder.FIFO_RESTING_ORDER_SIZE) * header.getBidsSize();

        byte[] bidBuffer = Arrays.copyOfRange(data, 880, (int) bidsSize);

        int offset = 0;
        offset += 16; // skip rbtree header
        offset += 8;  // Skip node allocator size

        int bumpIndex = PhoenixUtil.readInt32(bidBuffer, offset);
        offset += 4;

        int freeListHead = PhoenixUtil.readInt32(bidBuffer, offset);
        offset += 4;

        bidList = new ArrayList<>();
        bidListSanitized = new ArrayList<>();

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

            bidList.add(new Pair<>(fifoOrderId, fifoRestingOrder));
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

        var bidOrdersList = bidList;
        for (int i = 0; i < bidList.size(); i++) {
            Pair<FIFOOrderId, FIFORestingOrder> entry = bidOrdersList.get(i);
            if (!freeNodes.contains(i)) {
                // tree.set kv
                bidListSanitized.add(entry);
            }
        }

        return phoenixMarket;
   }
}
