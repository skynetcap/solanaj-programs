package com.mmorrell.phoenix.model;

import com.mmorrell.phoenix.util.PhoenixUtil;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        log.info("Bid size: " + bidsSize);
        byte[] bidBuffer = Arrays.copyOfRange(data, 880, (int) bidsSize);

        int offset = 0;
        offset += 16; // skip rbtree header
        // Skip node allocator size
        offset += 8;

        int bumpIndex = PhoenixUtil.readInt32(bidBuffer, offset);
        offset += 4;

        int freeListHead = PhoenixUtil.readInt32(bidBuffer, offset);
        offset += 4;

        log.info("Bump index: {}, freeListHead: {}", bumpIndex, freeListHead);

        Map<FIFOOrderId, FIFORestingOrder> orderMap = new HashMap<>();

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

            orderMap.put(fifoOrderId, fifoRestingOrder);
        }

        log.info("Order map: {}", orderMap);
        /**
         * for (let index = 0; offset < data.length && index < bumpIndex; index++) {
         *     let registers = new Array<number>();
         *     for (let i = 0; i < 4; i++) {
         *       registers.push(data.readInt32LE(offset)); // skip padding
         *       offset += 4;
         *     }
         *     let [key] = keyDeserializer.deserialize(
         *       data.subarray(offset, offset + keySize)
         *     );
         *     offset += keySize;
         *
         *     ////
         *
         *     let [value] = valueDeserializer.deserialize(
         *       data.subarray(offset, offset + valueSize)
         *     );
         *     offset += valueSize;
         *     nodes.push([key, value]);
         *     freeListPointers.push([index, registers[0]]);
         *   }
         */

        return phoenixMarket;
   }

}
