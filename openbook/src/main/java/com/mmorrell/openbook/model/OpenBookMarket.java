package com.mmorrell.openbook.model;

import com.mmorrell.openbook.OpenBookUtil;
import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    private double confFilter;
    private long maxStalenessSlots;
    private long quoteLotSize;
    private long baseLotSize;
    private long seqNum;
    private long registrationTime;
    private long makerFee;
    private long takerFee;
    private long feesAccrued;
    private long feesToReferrers;
    private long referrerRebatesAccrued;
    private long feesAvailable;
    private long makerVolume;
    private long takerVolume;
    private PublicKey baseMint;
    private PublicKey quoteMint;
    private PublicKey marketBaseVault;
    private long baseDepositTotal;
    private PublicKey marketQuoteVault;
    private long quoteDepositTotal;

    public static OpenBookMarket readOpenBookMarket(byte[] data, PublicKey marketId) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

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
                .confFilter(buffer.getDouble(360)) // 8 bytes
                .maxStalenessSlots(buffer.getLong(368)) // 8 bytes + 72 padding
                .quoteLotSize(Utils.readInt64(data, 448))
                .baseLotSize(Utils.readInt64(data, 456))
                .seqNum(Utils.readInt64(data, 464))
                .registrationTime(Utils.readInt64(data, 472))
                .makerFee(Utils.readInt64(data, 480))
                .takerFee(Utils.readInt64(data, 488))
                .feesAccrued(OpenBookUtil.readUint128(data, 496).longValue())
                .feesToReferrers(OpenBookUtil.readUint128(data, 512).longValue())
                .referrerRebatesAccrued(Utils.readInt64(data, 528))
                .feesAvailable(Utils.readInt64(data, 536))
                .makerVolume(OpenBookUtil.readUint128(data, 544).longValue())
                .takerVolume(OpenBookUtil.readUint128(data, 560).longValue())
                .baseMint(PublicKey.readPubkey(data, 576))
                .quoteMint(PublicKey.readPubkey(data, 608))
                .marketBaseVault(PublicKey.readPubkey(data, 640))
                .baseDepositTotal(Utils.readInt64(data, 672))
                .marketQuoteVault(PublicKey.readPubkey(data, 680))
                .quoteDepositTotal(Utils.readInt64(data, 712))
                .build();
    }

}
