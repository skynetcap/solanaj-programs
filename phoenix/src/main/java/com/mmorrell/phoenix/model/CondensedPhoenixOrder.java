package com.mmorrell.phoenix.model;

import lombok.Builder;
import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
@Builder
public class CondensedPhoenixOrder {

    private long priceInTicks;
    private long sizeInBaseLots;

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(18);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(0, priceInTicks);
        buffer.putLong(8, sizeInBaseLots);

        return buffer.array();
    }
}
