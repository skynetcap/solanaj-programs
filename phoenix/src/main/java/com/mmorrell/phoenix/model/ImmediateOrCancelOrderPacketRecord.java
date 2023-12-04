package com.mmorrell.phoenix.model;

import lombok.Builder;
import lombok.Data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
@Builder
public class ImmediateOrCancelOrderPacketRecord {

    private byte side;
    private long priceInTicks;
    private long numBaseLots;
    private long numQuoteLots;
    private long minBaseLotsToFill;
    private long minQuoteLotsToFill;
    private byte selfTradeBehavior;
    private long matchLimit;
    private byte[] clientOrderId;
    private boolean useOnlyDepositedFunds;

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(63);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put(0, side);
        buffer.put(1, (byte) 1);            // coption for present = 1
        buffer.putLong(2, priceInTicks == 0 ? (side == 0 ? 4503599627370496L : 0x00) : priceInTicks);
        buffer.putLong(10, numBaseLots);
        buffer.putLong(18, numQuoteLots);
        buffer.putLong(26, 0x00);     // min base lots to fill
        buffer.putLong(34, 0x00);     // quote lots to Fill
        buffer.put(42, selfTradeBehavior);
        buffer.put(43, (byte) 0);           // coption for not present = 0
//        buffer.putLong(44, matchLimit);         // skip matchLimit since it's coption is 0
        buffer.put(44, clientOrderId);      // go straight to clientOrderId from the coption 0 byte
        buffer.put(60, useOnlyDepositedFunds ? (byte) 1 : (byte) 0);
        buffer.put(61, (byte) 0);           // coption for lastValidSlot (0 = ignored)
        buffer.put(62, (byte) 0);           // coption for lastValidUnixTimestampInSeconds

        return buffer.array();
    }

}
