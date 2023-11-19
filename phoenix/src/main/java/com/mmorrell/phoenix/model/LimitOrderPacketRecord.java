package com.mmorrell.phoenix.model;

import lombok.Builder;
import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
@Builder
public class LimitOrderPacketRecord {

    private byte side;
    private long priceInTicks;
    private long numBaseLots;
    private byte selfTradeBehavior;
    private long matchLimit;
    private byte[] clientOrderId;
    private boolean useOnlyDepositedFunds;

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(26);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put(0, side);
        buffer.putLong(1, priceInTicks);
        buffer.putLong(9, numBaseLots);
        buffer.put(17, selfTradeBehavior);
        buffer.putLong(18, matchLimit);
        buffer.put(19, clientOrderId);

        return buffer.array();
    }

}
