package com.mmorrell.jupiter.model;

import com.mmorrell.jupiter.util.JupiterUtil;
import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Represents a Jupiter DCA (Dollar-Cost Averaging) account.
 */
@Data
@Builder
public class JupiterDca {
    private static final PublicKey USDC_MINT = new PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v");
    private static final PublicKey USDT_MINT = new PublicKey("Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB");
    private static final int DECIMALS = 6;

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

    // Not deserialized, but passed in from GPA
    private PublicKey publicKey;

    /**
     * Checks if the input mint is a stablecoin (USDC or USDT).
     *
     * @return true if the input mint is USDC or USDT, false otherwise.
     */
    public boolean isInputStablecoin() {
        return inputMint.equals(USDC_MINT) || inputMint.equals(USDT_MINT);
    }

    /**
     * Checks if the output mint is a stablecoin (USDC or USDT).
     *
     * @return true if the output mint is USDC or USDT, false otherwise.
     */
    public boolean isOutputStablecoin() {
        return outputMint.equals(USDC_MINT) || outputMint.equals(USDT_MINT);
    }

    /**
     * Calculates the USD value of the deposited amount.
     *
     * @return the USD value of the deposited amount, or null if neither input nor output is a stablecoin.
     */
    public BigDecimal getInDepositedUsd() {
        return isInputStablecoin() ? convertToUsd(inDeposited) : 
               (isOutputStablecoin() ? convertToUsd(outReceived) : null);
    }

    /**
     * Calculates the USD value of the withdrawn amount.
     *
     * @return the USD value of the withdrawn amount, or null if neither input nor output is a stablecoin.
     */
    public BigDecimal getInWithdrawnUsd() {
        return isInputStablecoin() ? convertToUsd(inWithdrawn) : 
               (isOutputStablecoin() ? convertToUsd(outWithdrawn) : null);
    }

    /**
     * Calculates the USD value of the used amount.
     *
     * @return the USD value of the used amount, or null if neither input nor output is a stablecoin.
     */
    public BigDecimal getInUsedUsd() {
        return isInputStablecoin() ? convertToUsd(inUsed) : 
               (isOutputStablecoin() ? convertToUsd(outReceived) : null);
    }

    /**
     * Calculates the USD value of the amount per cycle.
     *
     * @return the USD value of the amount per cycle, or null if neither input nor output is a stablecoin.
     */
    public BigDecimal getInAmountPerCycleUsd() {
        return isInputStablecoin() ? convertToUsd(inAmountPerCycle) : null;
    }

    /**
     * Calculates the total notional value in the original token.
     *
     * @return the total notional value in the original token.
     */
    public BigDecimal getTotalNotional() {
        return convertToDecimal(inDeposited);
    }

    /**
     * Calculates the total notional value in USD.
     *
     * @return the total notional value in USD, or null if neither input nor output is a stablecoin.
     */
    public BigDecimal getTotalNotionalUsd() {
        return isInputStablecoin() ? convertToUsd(inDeposited) : 
               (isOutputStablecoin() ? convertToUsd(outReceived) : null);
    }

    /**
     * Calculates the remaining notional value in the original token.
     *
     * @return the remaining notional value in the original token.
     */
    public BigDecimal getRemainingNotional() {
        return convertToDecimal(inDeposited - inUsed);
    }

    /**
     * Calculates the remaining notional value in USD.
     *
     * @return the remaining notional value in USD, or null if neither input nor output is a stablecoin.
     */
    public BigDecimal getRemainingNotionalUsd() {
        if (isInputStablecoin()) {
            return convertToUsd(inDeposited - inUsed);
        } else if (isOutputStablecoin()) {
            return convertToUsd(outReceived - outWithdrawn);
        }
        return null;
    }

    /**
     * Converts a token amount to its USD value.
     *
     * @param amount the token amount to convert.
     * @return the USD value of the token amount.
     */
    private BigDecimal convertToUsd(long amount) {
        return BigDecimal.valueOf(amount)
                .divide(BigDecimal.valueOf(Math.pow(10, DECIMALS)), 2, RoundingMode.HALF_UP);
    }

    /**
     * Converts a token amount to its decimal representation.
     *
     * @param amount the token amount to convert.
     * @return the decimal representation of the token amount.
     */
    private BigDecimal convertToDecimal(long amount) {
        return BigDecimal.valueOf(amount)
                .divide(BigDecimal.valueOf(Math.pow(10, DECIMALS)), DECIMALS, RoundingMode.HALF_UP);
    }

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