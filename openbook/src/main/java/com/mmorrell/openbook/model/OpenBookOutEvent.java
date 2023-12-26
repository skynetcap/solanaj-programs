package com.mmorrell.openbook.model;

import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * OpenBookOutEvent represents an event of a Maker action (i.e. Cxl) for a particular token and side.
 */
@Data
@Builder
public class OpenBookOutEvent {

    private byte eventType;
    private byte side;
    private byte ownerSlot;
    private long timestamp;
    private long seqNum;
    private PublicKey owner;
    private long quantity;

    /**
     * Reads an OpenBookOutEvent from the given byte array.
     *
     * @param data the byte array containing the OpenBookOutEvent data
     * @return the deserialized OpenBookOutEvent object
     */
    public static OpenBookOutEvent readOpenBookOutEvent(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        byte eventType = buffer.get();
        byte side = buffer.get();
        byte ownerSlot = buffer.get();

        buffer.position(buffer.position() + 5); // Skip 5 bytes of padding

        long timestamp = buffer.getLong();
        long seqNum = buffer.getLong();
        byte[] ownerBytes = new byte[32]; // PublicKey byte array size is 32
        buffer.get(ownerBytes);
        PublicKey owner = new PublicKey(ownerBytes);
        long quantity = buffer.getLong();

        buffer.position(buffer.position() + 80); // Skip 80 bytes of padding

        return OpenBookOutEvent.builder()
                .eventType(eventType)
                .side(side)
                .ownerSlot(ownerSlot)
                .timestamp(timestamp)
                .seqNum(seqNum)
                .owner(owner)
                .quantity(quantity)
                .build();
    }
}