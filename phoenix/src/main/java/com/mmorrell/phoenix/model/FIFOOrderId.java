package com.mmorrell.phoenix.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FIFOOrderId {

    public static final int FIFO_ORDER_ID_SIZE = 16;
    private long priceInTicks;
    private long orderSequenceNumber;

}
