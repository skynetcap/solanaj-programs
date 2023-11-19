package com.mmorrell.phoenix.model;

import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;

@Data
@Builder
public class FIFORestingOrder {

    public static final int FIFO_RESTING_ORDER_SIZE = 32;
    private long traderIndex;
    private long numBaseLots;

    // 16 bytes of padding

    public static FIFORestingOrder readFifoRestingOrder(byte[] data) {
        return FIFORestingOrder.builder()
                .traderIndex(Utils.readInt64(data, 0))
                .numBaseLots(Utils.readInt64(data, 8))
                .build();
    }

}
