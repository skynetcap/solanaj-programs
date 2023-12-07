package com.mmorrell.openbook.model;

import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;

@Data
@Builder
public class OpenBookMarket {

    private PublicKey marketId;
    private byte bump;
    private byte baseDecimals, quoteDecimals;
    private PublicKey marketAuthority;
    private long timeExpiry;
    private PublicKey collectFeeAdmin;

    public static OpenBookMarket readOpenBookMarket(byte[] data, PublicKey marketId) {
        return OpenBookMarket.builder()
                .marketId(marketId)
                .bump(data[8])
                .baseDecimals(data[9])
                .quoteDecimals(data[10])
                .marketAuthority(PublicKey.readPubkey(data, 16))
                .timeExpiry(Utils.readInt64(data, 48))
                .collectFeeAdmin(PublicKey.readPubkey(data, 56))
                .build();
    }

}
