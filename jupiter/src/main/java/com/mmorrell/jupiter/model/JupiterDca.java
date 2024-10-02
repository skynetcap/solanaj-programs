package com.mmorrell.jupiter.model;

import com.mmorrell.jupiter.util.JupiterUtil;
import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;

/**
 * Represents a Jupiter DCA (Dollar-Cost Averaging) account.
 */
@Data
@Builder
public class JupiterDca {
    private PublicKey user;
    private PublicKey inputMint;
    private PublicKey outputMint;
    private long idx;
    private long nextCycleAt;
    private long inDeposited;
    private long inWithdrawn;
    private long outWithdrawn;
    private long inUsed;
    private long outReceived;
    private long inAmountPerCycle;
    private long cycleFrequency;
    private long nextCycleAmountLeft;
    private PublicKey inAccount;
    private PublicKey outAccount;
    private long minOutAmount;
    private long maxOutAmount;
    private long keeperInBalanceBeforeBorrow;
    private long dcaOutBalanceBeforeSwap;
    private long createdAt;
    private byte bump;

    /**
     * Deserializes a byte array into a JupiterDca object.
     *
     * @param data the byte array to deserialize.
     * @return the deserialized JupiterDca object.
     */
    public static JupiterDca fromByteArray(byte[] data) {
        int offset = 8;

        PublicKey user = PublicKey.readPubkey(data, offset);
        offset += 32;

        PublicKey inputMint = PublicKey.readPubkey(data, offset);
        offset += 32;

        PublicKey outputMint = PublicKey.readPubkey(data, offset);
        offset += 32;

        long idx = readLong(data, offset);
        offset += 8;

        long nextCycleAt = readLong(data, offset);
        offset += 8;

        long inDeposited = readLong(data, offset);
        offset += 8;

        long inWithdrawn = readLong(data, offset);
        offset += 8;

        long outWithdrawn = readLong(data, offset);
        offset += 8;

        long inUsed = readLong(data, offset);
        offset += 8;

        long outReceived = readLong(data, offset);
        offset += 8;

        long inAmountPerCycle = readLong(data, offset);
        offset += 8;

        long cycleFrequency = readLong(data, offset);
        offset += 8;

        long nextCycleAmountLeft = readLong(data, offset);
        offset += 8;

        PublicKey inAccount = PublicKey.readPubkey(data, offset);
        offset += 32;

        PublicKey outAccount = PublicKey.readPubkey(data, offset);
        offset += 32;

        long minOutAmount = readLong(data, offset);
        offset += 8;

        long maxOutAmount = readLong(data, offset);
        offset += 8;

        long keeperInBalanceBeforeBorrow = readLong(data, offset);
        offset += 8;

        long dcaOutBalanceBeforeSwap = readLong(data, offset);
        offset += 8;

        long createdAt = readLong(data, offset);
        offset += 8;

        byte bump = data[offset];

        return JupiterDca.builder()
                .user(user)
                .inputMint(inputMint)
                .outputMint(outputMint)
                .idx(idx)
                .nextCycleAt(nextCycleAt)
                .inDeposited(inDeposited)
                .inWithdrawn(inWithdrawn)
                .outWithdrawn(outWithdrawn)
                .inUsed(inUsed)
                .outReceived(outReceived)
                .inAmountPerCycle(inAmountPerCycle)
                .cycleFrequency(cycleFrequency)
                .nextCycleAmountLeft(nextCycleAmountLeft)
                .inAccount(inAccount)
                .outAccount(outAccount)
                .minOutAmount(minOutAmount)
                .maxOutAmount(maxOutAmount)
                .keeperInBalanceBeforeBorrow(keeperInBalanceBeforeBorrow)
                .dcaOutBalanceBeforeSwap(dcaOutBalanceBeforeSwap)
                .createdAt(createdAt)
                .bump(bump)
                .build();
    }

    /**
     * Reads a long value from the byte array at the specified offset.
     *
     * @param data   the byte array.
     * @param offset the offset to start reading from.
     * @return the long value.
     */
    private static long readLong(byte[] data, int offset) {
        return ((long) (data[offset] & 0xFF)) |
                (((long) (data[offset + 1] & 0xFF)) << 8) |
                (((long) (data[offset + 2] & 0xFF)) << 16) |
                (((long) (data[offset + 3] & 0xFF)) << 24) |
                (((long) (data[offset + 4] & 0xFF)) << 32) |
                (((long) (data[offset + 5] & 0xFF)) << 40) |
                (((long) (data[offset + 6] & 0xFF)) << 48) |
                (((long) (data[offset + 7] & 0xFF)) << 56);
    }
}