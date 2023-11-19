package com.mmorrell.phoenix.model;

import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;

@Data
@Builder
public class FIFOOrderId {

    public static final int FIFO_ORDER_ID_SIZE = 16;
    private long priceInTicks;
    private long orderSequenceNumber;

    public static FIFOOrderId readFifoOrderId(byte[] data) {
        return FIFOOrderId.builder()
                .priceInTicks(Utils.readInt64(data, 0))
                .orderSequenceNumber(Utils.readInt64(data, 8))
                .build();
    }

}
