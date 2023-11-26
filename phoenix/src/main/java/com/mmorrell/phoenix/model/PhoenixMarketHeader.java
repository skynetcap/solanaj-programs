package com.mmorrell.phoenix.model;

import com.mmorrell.phoenix.util.PhoenixUtil;
import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;

@Data
@Builder
public class PhoenixMarketHeader {

    public static final int MARKET_HEADER_SIZE = 328;
    private long discriminant;
    private long status;
    private long bidsSize;
    private long asksSize;
    private long numSeats;

    // Token params

    // Base token
    private int baseDecimals;
    private int baseVaultBump;
    private PublicKey baseMintKey;
    private PublicKey baseVaultKey;
    private long baseLotSize;

    // Quote token
    private int quoteDecimals;
    private int quoteVaultBump;
    private PublicKey quoteMintKey;
    private PublicKey quoteVaultKey;
    private long quoteLotSize;

    // Metadata
    private long tickSize;
    private PublicKey authority;
    private PublicKey feeDestination;
    private long marketSequenceNumber;
    private PublicKey successor;
    private long rawBaseUnitsPerBaseUnit;

    public static PhoenixMarketHeader readPhoenixMarketHeader(byte[] data) {
        return PhoenixMarketHeader.builder()
                .discriminant(Utils.readInt64(data, 0))
                .status(Utils.readInt64(data, 8))
                .bidsSize(Utils.readInt64(data, 16))
                .asksSize(Utils.readInt64(data, 24))
                .numSeats(Utils.readInt64(data, 32))
                .baseDecimals(PhoenixUtil.readInt32(data, 40))
                .baseVaultBump(PhoenixUtil.readInt32(data, 44))
                .baseMintKey(PublicKey.readPubkey(data, 48))
                .baseVaultKey(PublicKey.readPubkey(data, 80))
                .baseLotSize(Utils.readInt64(data, 112))
                .quoteDecimals(PhoenixUtil.readInt32(data, 120))
                .quoteVaultBump(PhoenixUtil.readInt32(data, 124))
                .quoteMintKey(PublicKey.readPubkey(data, 128))
                .quoteVaultKey(PublicKey.readPubkey(data, 160))
                .quoteLotSize(Utils.readInt64(data, 192))
                .tickSize(Utils.readInt64(data, 200))
                .authority(PublicKey.readPubkey(data, 208))
                .feeDestination(PublicKey.readPubkey(data, 240))
                .marketSequenceNumber(Utils.readInt64(data, 272))
                .successor(PublicKey.readPubkey(data, 280))
                .rawBaseUnitsPerBaseUnit(PhoenixUtil.readInt32(data, 312))
                .build();
    }

}