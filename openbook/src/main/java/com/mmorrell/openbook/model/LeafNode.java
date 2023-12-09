package com.mmorrell.openbook.model;

import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;

import java.nio.ByteBuffer;
import java.util.Arrays;

@Data
@Builder
public class LeafNode {

    private int tag;
    private byte ownerSlot;
    private int timeInForce;
    // 4 bytes padding, now at offset 6
    private byte[] key;
    private PublicKey owner;
    private long quantity;
    private long timestamp;
    private long pegLimit;
    private long clientOrderId;

    private long price;

    public static LeafNode readLeafNode(AnyNode anyNode) {
        byte[] data = anyNode.getData(); // starts at offset 1 of 88
        byte tag = anyNode.getTag(); // ignore offset 0
        byte ownerSlot = data[0];
        int timeInForce = Utils.readUint16(data, 1);
        byte[] key = Arrays.copyOfRange(data, 6, 22);
        PublicKey owner = PublicKey.readPubkey(data, 23);
        long quantity = Utils.readInt64(data, 55);
        long timeStamp = Utils.readInt64(data, 63);
        long pegLimit = Utils.readInt64(data, 71);
        long clientOrderId = Utils.readInt64(data, 79);
        long price = Utils.readInt64(ByteBuffer.allocate(17).put(key).array(), 9);

        return LeafNode.builder()
                .tag(tag)
                .ownerSlot(ownerSlot)
                .timeInForce(timeInForce)
                .key(key)
                .owner(owner)
                .quantity(quantity)
                .timestamp(timeStamp)
                .pegLimit(pegLimit)
                .clientOrderId(clientOrderId)
                .price(price)
                .build();
    }
}
