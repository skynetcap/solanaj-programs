package com.mmorrell.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;

@Builder
@Getter
@Setter
@ToString
public class MangoTokenInfo {

    public static final int MANGO_TOKEN_INFO_LAYOUT_SIZE = (PublicKey.PUBLIC_KEY_LENGTH * 2) + 8;
    private static final int MINT_OFFSET = 0;
    private static final int ROOT_BANK_OFFSET = MINT_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int DECIMALS_OFFSET = ROOT_BANK_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;

    private PublicKey mint;
    private PublicKey rootBank;
    private byte decimals;

    public static MangoTokenInfo readMangoTokenInfo(byte[] data) {
        return MangoTokenInfo.builder()
                .mint(
                        PublicKey.readPubkey(data, MINT_OFFSET)
                )
                .rootBank(
                        PublicKey.readPubkey(data, ROOT_BANK_OFFSET)
                )
                .decimals(
                        data[DECIMALS_OFFSET]
                )
                .build();
    }
}
