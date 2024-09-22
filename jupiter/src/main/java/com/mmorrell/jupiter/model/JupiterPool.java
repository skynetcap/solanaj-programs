package com.mmorrell.jupiter.model;

import com.mmorrell.openbook.OpenBookUtil;
import lombok.Builder;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.p2p.solanaj.core.PublicKey;

import static com.mmorrell.openbook.OpenBookUtil.readInt32;

/**
 * Represents a Jupiter Perpetuals Pool account.
 */
@Data
@Builder
public class JupiterPool {
    private String name;
    private List<PublicKey> custodies;
    private long aumUsd;
    private Limit limit;
    private Fees fees;
    private PoolApr poolApr;
    private long maxRequestExecutionSec;
    private byte bump;
    private byte lpTokenBump;
    private long inceptionTime;

    @Data
    @Builder
    public static class Limit {
        private long maxAumUsd;
        private long tokenWeightageBufferBps;
        private long maxPositionUsd;
    }

    @Data
    @Builder
    public static class Fees {
        private long increasePositionBps;
        private long decreasePositionBps;
        private long addRemoveLiquidityBps;
        private long swapBps;
        private long taxBps;
        private long stableSwapBps;
        private long stableSwapTaxBps;
        private long liquidationRewardBps;
        private long protocolShareBps;
    }

    @Data
    @Builder
    public static class PoolApr {
        private long lastUpdated;
        private long feeAprBps;
        private long realizedFeeUsd;
    }

    /**
     * Deserializes a byte array into a JupiterPool object.
     *
     * @param data the byte array representing the account data.
     * @return a JupiterPool object.
     */
    public static JupiterPool fromByteArray(byte[] data) {
        int offset = 8; // Skip discriminator

        String name = readString(data, offset);
        offset += 4 + name.length(); // 4 bytes for length + actual string length

        List<PublicKey> custodies = readPublicKeyList(data, offset);
        offset += 4 + (custodies.size() * 32); // 4 bytes for vector length + 32 bytes per PublicKey

        long aumUsd = readUint128(data, offset);
        offset += 16;

        Limit limit = readLimit(data, offset);
        offset += 40; // 16 + 16 + 8 bytes each for maxAumUsd, tokenWeightageBufferBps, maxPositionUsd

        Fees fees = readFees(data, offset);
        offset += 72; // 8 bytes each for 9 fee fields

        PoolApr poolApr = readPoolApr(data, offset);
        offset += 24;

        long maxRequestExecutionSec = readInt64(data, offset);
        offset += 8;

        byte bump = data[offset++];
        byte lpTokenBump = data[offset++];

        long inceptionTime = readInt64(data, offset);

        return JupiterPool.builder()
                .name(name)
                .custodies(custodies)
                .aumUsd(aumUsd)
                .limit(limit)
                .fees(fees)
                .poolApr(poolApr)
                .maxRequestExecutionSec(maxRequestExecutionSec)
                .bump(bump)
                .lpTokenBump(lpTokenBump)
                .inceptionTime(inceptionTime)
                .build();
    }

    private static String readString(byte[] data, int offset) {
        int length = readInt32(data, offset);
        offset += 4;
        byte[] stringBytes = Arrays.copyOfRange(data, offset, offset + length);
        return new String(stringBytes, StandardCharsets.UTF_8);
    }

    private static List<PublicKey> readPublicKeyList(byte[] data, int offset) {
        List<PublicKey> custodies = new ArrayList<>();
        int vectorLength = readInt32(data, offset);
        offset += 4; // Move past the vector length
        for (int i = 0; i < vectorLength; i++) {
            PublicKey custody = PublicKey.readPubkey(data, offset);
            custodies.add(custody);
            offset += 32; // Move to the next PublicKey
        }
        return custodies;
    }

    private static long readUint128(byte[] data, int offset) {
        return OpenBookUtil.readUint128(data, offset).longValue();
    }

    private static Limit readLimit(byte[] data, int offset) {
        return Limit.builder()
                .maxAumUsd(readUint128(data, offset))
                .tokenWeightageBufferBps(readUint128(data, offset + 16))
                .maxPositionUsd(readUint64(data, offset + 32))
                .build();
    }

    private static Fees readFees(byte[] data, int offset) {
        return Fees.builder()
                .increasePositionBps(readUint64(data, offset))
                .decreasePositionBps(readUint64(data, offset + 8))
                .addRemoveLiquidityBps(readUint64(data, offset + 16))
                .swapBps(readUint64(data, offset + 24))
                .taxBps(readUint64(data, offset + 32))
                .stableSwapBps(readUint64(data, offset + 40))
                .stableSwapTaxBps(readUint64(data, offset + 48))
                .liquidationRewardBps(readUint64(data, offset + 56))
                .protocolShareBps(readUint64(data, offset + 64))
                .build();
    }

    private static PoolApr readPoolApr(byte[] data, int offset) {
        return PoolApr.builder()
                .lastUpdated(readInt64(data, offset))
                .feeAprBps(readInt64(data, offset + 8))
                .realizedFeeUsd(readInt64(data, offset + 16))
                .build();
    }

    private static long readInt64(byte[] data, int offset) {
        return org.bitcoinj.core.Utils.readInt64(data, offset);
    }

    private static long readUint64(byte[] data, int offset) {
        return Long.parseUnsignedLong(Long.toUnsignedString(org.bitcoinj.core.Utils.readInt64(data, offset)));
    }
}