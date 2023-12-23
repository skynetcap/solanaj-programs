package com.mmorrell.openbook.model;

import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.utils.ByteUtils;

@Data
@Builder
public class OpenBookFillEvent {

    private byte eventType;
    private byte takerSide;
    private byte makerOut;
    private byte makerSlot;

    // 4 bytes padding
    private long timeStamp;
    private long seqNum;
    private PublicKey maker;
    private long makerTimeStamp;
    private PublicKey taker;
    private long takerClientOrderId;
    private long price;
    private long pegLimit;
    private long quantity;
    private long makerClientOrderId;
    // 8 bytes reserved padding

    // Custom
    private PublicKey makerOwner;
    private PublicKey takerOwner;
    private double priceDouble;
    private double quantityDouble;

    public static OpenBookFillEvent readOpenBookFillEvent(byte[] data) {
        return OpenBookFillEvent.builder()
                .eventType((byte) 0)
                .takerSide(data[1])
                .makerOut(data[2])
                .makerSlot(data[3])
                .timeStamp(ByteUtils.readUint64(data, 8).longValue())
                .seqNum(ByteUtils.readUint64(data, 16).longValue())
                .maker(PublicKey.readPubkey(data, 24))
                .makerTimeStamp(ByteUtils.readUint64(data, 56).longValue())
                .taker(PublicKey.readPubkey(data, 64))
                .takerClientOrderId(ByteUtils.readUint64(data, 96).longValue())
                .price(ByteUtils.readUint64(data, 104).longValue())
                .pegLimit(ByteUtils.readUint64(data, 112).longValue())
                .quantity(ByteUtils.readUint64(data, 120).longValue())
                .makerClientOrderId(ByteUtils.readUint64(data, 128).longValue())
                .build();
    }

}
