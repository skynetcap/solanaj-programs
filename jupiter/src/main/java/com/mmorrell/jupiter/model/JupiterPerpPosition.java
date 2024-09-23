package com.mmorrell.jupiter.model;

import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;

/**
 * 8 (padding) +
 * 32 (owner) +
 * 32 (pool) +
 * 32 (custody) +
 * 32 (collateralCustody) +
 * 8 (openTime) +
 * 8 (updateTime) +
 * 4 (side) +
 * 8 (price) +
 * 8 (sizeUsd) +
 * 8 (collateralUsd) +
 * 8 (realisedPnlUsd) +
 * 8 (cumulativeInterestSnapshot) +
 * 8 (lockedAmount) +
 * 4 (bump) =  8 + 128 + 64 + 4 + 4 =  216 bytes
 */
@Data
@Builder
public class JupiterPerpPosition {
    private PublicKey owner;
    private PublicKey pool;
    private PublicKey custody;
    private PublicKey collateralCustody;
    private long openTime;
    private long updateTime;
    private Side side;
    private long price;
    private long sizeUsd;
    private long collateralUsd;
    private long realisedPnlUsd;
    private long cumulativeInterestSnapshot;
    private long lockedAmount;
    private byte bump;

    public enum Side {
        LONG,
        SHORT
    }

    public static JupiterPerpPosition fromByteArray(byte[] data) {
        int offset = 8; // Start at offset 8 to skip the padding
        return JupiterPerpPosition.builder()
                .owner(PublicKey.readPubkey(data, offset))
                .pool(PublicKey.readPubkey(data, offset += 32))
                .custody(PublicKey.readPubkey(data, offset += 32))
                .collateralCustody(PublicKey.readPubkey(data, offset += 32))
                .openTime(readInt64(data, offset += 32))
                .updateTime(readInt64(data, offset += 8))
                .side(data[offset += 8] == 1 ? Side.LONG : Side.SHORT)
                .price(readUint64(data, offset += 1))
                .sizeUsd(readUint64(data, offset += 8))
                .collateralUsd(readUint64(data, offset += 8))
                .realisedPnlUsd(readInt64(data, offset += 8))
                .cumulativeInterestSnapshot(readUint128(data, offset += 8))
                .lockedAmount(readUint64(data, offset += 16))
                .bump(data[offset += 8])
                .build();
    }

    private static long readInt64(byte[] data, int offset) {
        return org.bitcoinj.core.Utils.readInt64(data, offset);
    }

    private static long readUint64(byte[] data, int offset) {
        return org.bitcoinj.core.Utils.readInt64(data, offset);
    }

    private static long readUint128(byte[] data, int offset) {
        return org.bitcoinj.core.Utils.readInt64(data, offset);
    }
}