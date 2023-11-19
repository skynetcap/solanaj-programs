package com.mmorrell.phoenix.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FIFORestingOrder {

    public static final int FIFO_RESTING_ORDER_SIZE = 32;
    private long traderIndex;
    private long numBaseLots;

    // 16 bytes of padding

}
