package com.mmorrell.openbook.model;

import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.utils.ByteUtils;

/**
 * Represents an open book open orders account.
 *
 * This class is used to store information about an open book open orders account, which includes details such as the owner,
 * market, name, delegate, account number, bump, and padding. It also provides a method to read an open book open orders account
 * from raw byte data.
 */
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

    /**
     * Reads an OpenBookOpenOrdersAccount from the given byte data.
     *
     * @param data The byte data to read from.
     * @return The OpenBookOpenOrdersAccount object.
     */
    public static OpenBookOpenOrdersAccount readOpenBookOpenOrdersAccount(byte[] data) {
        return OpenBookOpenOrdersAccount.builder()
                .owner(PublicKey.readPubkey(data, 8))
                .market(PublicKey.readPubkey(data, 40))
                .name(new String(ByteUtils.readBytes(data, 72, 32)))
                .build();
    }

}
