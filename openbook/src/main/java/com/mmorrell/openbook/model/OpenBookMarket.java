package com.mmorrell.openbook.model;

import com.google.common.primitives.Bytes;
import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Data
@Builder
public class OpenBookMarket {

    private PublicKey marketId;
    private byte bump;
    private byte baseDecimals, quoteDecimals;
    private PublicKey marketAuthority;
    private long timeExpiry;
    private PublicKey collectFeeAdmin;
    private PublicKey openOrdersAdmin;
    private PublicKey consumeEventsAdmin;
    private PublicKey closeMarketAdmin;
    private String name;
    private PublicKey bids;
    private PublicKey asks;
    private PublicKey eventHeap;
    private PublicKey oracleA;
    private PublicKey oracleB;

    public static OpenBookMarket readOpenBookMarket(byte[] data, PublicKey marketId) {
        return OpenBookMarket.builder()
                .marketId(marketId)
                .bump(data[8])
                .baseDecimals(data[9])
                .quoteDecimals(data[10])
                .marketAuthority(PublicKey.readPubkey(data, 16))
                .timeExpiry(Utils.readInt64(data, 48))
                .collectFeeAdmin(PublicKey.readPubkey(data, 56))
                .openOrdersAdmin(PublicKey.readPubkey(data, 88))
                .consumeEventsAdmin(PublicKey.readPubkey(data, 120))
                .closeMarketAdmin(PublicKey.readPubkey(data, 152))
                .name(new String(Arrays.copyOfRange(data, 184, 200), StandardCharsets.UTF_8).trim())
                .bids(PublicKey.readPubkey(data, 200))
                .asks(PublicKey.readPubkey(data, 232))
                .eventHeap(PublicKey.readPubkey(data, 264))
                .oracleA(PublicKey.readPubkey(data, 296))
                .oracleB(PublicKey.readPubkey(data, 328))
                .build();
    }

}
