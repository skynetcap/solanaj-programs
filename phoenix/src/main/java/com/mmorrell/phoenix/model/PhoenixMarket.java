package com.mmorrell.phoenix.model;

import com.mmorrell.phoenix.util.PhoenixUtil;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Utils;

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

    @Getter
    public static Map<FIFOOrderId, FIFORestingOrder> bidOrders;

    @Getter
    public static Map<FIFOOrderId, FIFORestingOrder> bidOrdersSanitized;

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

        // log.info("Bid size: " + bidsSize);
        byte[] bidBuffer = Arrays.copyOfRange(data, 880, (int) bidsSize);

        int offset = 0;
        offset += 16; // skip rbtree header
        // Skip node allocator size
        offset += 8;

        int bumpIndex = PhoenixUtil.readInt32(bidBuffer, offset);
        offset += 4;

        int freeListHead = PhoenixUtil.readInt32(bidBuffer, offset);
        offset += 4;

        // log.info("Bump index: {}, freeListHead: {}", bumpIndex, freeListHead);
        bidOrders = new HashMap<>();
        bidOrdersSanitized = new HashMap<>();

        Map<Integer, Integer> freeListPointers = new HashMap<>();

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

            bidOrders.put(fifoOrderId, fifoRestingOrder);

            freeListPointers.put(index, registers.get(0));
        }

        Set<Integer> freeNodes = new HashSet<>();
        int indexToRemove = freeListHead - 1;
        int counter = 0;

        while (freeListHead != 0) {
            var next = freeListPointers.get(freeListHead - 1);
            indexToRemove = next;
            freeListHead = next;

            freeNodes.add(indexToRemove);
            counter += 1;

            if (counter > bumpIndex) {
                log.error("Infinite Loop Detected");
            }

        }

        for (int i = 0; i < bidOrders.size(); i++) {
            Map.Entry<FIFOOrderId, FIFORestingOrder> entry = bidOrders.entrySet().stream().toList().get(i);
            if (!freeNodes.contains(i)) {
                // tree.set kv
                bidOrdersSanitized.put(entry.getKey(), entry.getValue());
            }
        }

        log.info("Sanitized: " + bidOrdersSanitized.toString());
        /**
         *   let freeNodes = new Set<number>();
         *   let indexToRemove = freeListHead - 1;
         *   let counter = 0;
         *   // If there's an infinite loop here, that means that the state is corrupted
         *   while (freeListHead !== 0) {
         *     // We need to subtract 1 because the node allocator is 1-indexed
         *     let next = freeListPointers[freeListHead - 1];
         *     [indexToRemove, freeListHead] = next;
         *     freeNodes.add(indexToRemove);
         *     counter += 1;
         *     if (counter > bumpIndex) {
         *       throw new Error("Infinite loop detected");
         *     }
         *   }
         *
         *   for (let [index, [key, value]] of nodes.entries()) {
         *     if (!freeNodes.has(index)) {
         *       tree.set(key, value);
         *     }
         *   }
         */

        return phoenixMarket;
   }
}
