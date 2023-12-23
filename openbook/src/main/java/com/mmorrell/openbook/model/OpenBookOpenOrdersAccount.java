package com.mmorrell.openbook.model;

import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.utils.ByteUtils;

@Data
@Builder
public class OpenBookOpenOrdersAccount {

    private PublicKey owner;
    private PublicKey market;
    private String name; // 32 bytes
    private PublicKey delegate;
    private int accountNum;
    private byte bump;
    // 3 bytes of padding
    private byte[] padding;

    // position x 1 DTO TBD
    // openOrders x 24 DTO TBD

    public static OpenBookOpenOrdersAccount readOpenBookOpenOrdersAccount(byte[] data) {
        return OpenBookOpenOrdersAccount.builder()
                .owner(PublicKey.readPubkey(data, 8))
                .market(PublicKey.readPubkey(data, 40))
                .name(new String(ByteUtils.readBytes(data, 72, 32)))
                .build();
    }

}
