package com.mmorrell.mango.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.p2p.solanaj.core.PublicKey;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MangoSlabLeafNode extends MangoSlabNode {

    private byte ownerSlot;
    private byte orderType;
    private byte version;
    private byte timeInForce;
    private byte[] key;
    private PublicKey owner;
    private long quantity;
    private long clientOrderId;
    private long price;
    private long bestInitial;
    private long timestamp;

}
