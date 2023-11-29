package com.mmorrell.phoenix.model;

import lombok.Builder;
import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class MultipleOrderPacketRecord {

    private List<CondensedPhoenixOrder> bids;
    private List<CondensedPhoenixOrder> asks;


    public byte[] toBytes() {
        // estimate
        int bufferSize = getBufferSize();  // 1 for coption clientOrderId, 1 for enum

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        int offset = 0;
        buffer.putInt(offset, bids.size());
        offset += 4;

        // bids
        for (CondensedPhoenixOrder bid : bids) {
            buffer.put(offset, bid.toBytes());
            offset += 18;
        }

        buffer.putInt(offset, (byte) asks.size());
        offset += 4;

        // asks
        for (CondensedPhoenixOrder ask : asks) {
            buffer.put(offset, ask.toBytes());
            offset += 18;
        }

        buffer.put(offset, (byte) 0);
        offset += 1;

        buffer.put(offset, (byte) 2);

        return buffer.array();
    }

    public int getBufferSize() {
        int bidsBufferSize = bids.size() * 18;
        int asksBufferSize = asks.size() * 18;
        int bufferSize = bidsBufferSize + asksBufferSize + 1 + 1 + 8;

        return bufferSize;
    }

}
