package com.mmorrell.phoenix.model;

import com.google.common.io.Files;
import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Data
@Builder
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

        byte[] bidBuffer = Arrays.copyOfRange(data, 880, (int) bidsSize);

        return phoenixMarket;
   }

}
