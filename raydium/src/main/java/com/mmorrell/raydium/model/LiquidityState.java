package com.mmorrell.raydium.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.utils.ByteUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public record LiquidityState(BigInteger u64Status, BigInteger u64Nonce, BigInteger u64MaxOrder, BigInteger u64Depth,
                             BigInteger u64BaseDecimal, BigInteger u64QuoteDecimal, BigInteger u64State,
                             BigInteger u64ResetFlag, BigInteger u64MinSize, BigInteger u64VolMaxCutRatio,
                             BigInteger u64AmountWaveRatio, BigInteger u64BaseLotSize, BigInteger u64QuoteLotSize,
                             BigInteger u64MinPriceMultiplier, BigInteger u64MaxPriceMultiplier,
                             BigInteger u64SystemDecimalValue, BigInteger u64MinSeparateNumerator,
                             BigInteger u64MinSeparateDenominator, BigInteger u64TradeFeeNumerator,
                             BigInteger u64TradeFeeDenominator, BigInteger u64PnlNumerator,
                             BigInteger u64PnlDenominator, BigInteger u64SwapFeeNumerator,
                             BigInteger u64SwapFeeDenominator, BigInteger u64BaseNeedTakePnl,
                             BigInteger u64QuoteNeedTakePnl, BigInteger u64QuoteTotalPnl, BigInteger u64BaseTotalPnl,
                             BigInteger u64PoolOpenTime, BigInteger u64PunishPcAmount, BigInteger u64PunishCoinAmount,
                             BigInteger u64OrderbookToInitTime, BigInteger u128SwapBaseInAmount,
                             BigInteger u128SwapQuoteOutAmount, BigInteger u64SwapBase2QuoteFee,
                             BigInteger u128SwapQuoteInAmount, BigInteger u128SwapBaseOutAmount,
                             BigInteger u64SwapQuote2BaseFee, PublicKey baseVault, PublicKey quoteVault,
                             PublicKey baseMint, PublicKey quoteMint, PublicKey lpMint, PublicKey openOrders,
                             PublicKey marketId, PublicKey marketProgramId, PublicKey targetOrders,
                             PublicKey withdrawQueue, PublicKey lpVault, PublicKey owner, BigInteger u64LpReserve,
                             List<BigInteger> u64Padding) {
    public static LiquidityState decode(byte[] data) {
        MutableInt offset = new MutableInt(0);
        return new LiquidityState(
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint64(data, offset),
                readUint128(data, offset),
                readUint128(data, offset),
                readUint64(data, offset),
                readUint128(data, offset),
                readUint128(data, offset),
                readUint64(data, offset),
                readPubkey(data, offset),
                readPubkey(data, offset),
                readPubkey(data, offset),
                readPubkey(data, offset),
                readPubkey(data, offset),
                readPubkey(data, offset),
                readPubkey(data, offset),
                readPubkey(data, offset),
                readPubkey(data, offset),
                readPubkey(data, offset),
                readPubkey(data, offset),
                readPubkey(data, offset),
                readUint64(data, offset),
                readU64PaddingList(data, offset)
        );
    }

    private static BigInteger readUint64(byte[] data, MutableInt offset) {
        BigInteger value = ByteUtils.readUint64(data, offset.getValue());
        offset.setValue(offset.getValue() + ByteUtils.UINT_64_LENGTH);
        return value;
    }

    private static BigInteger readUint128(byte[] data, MutableInt offset) {
        BigInteger value = ByteUtils.readUint128(data, offset.getValue());
        offset.setValue(offset.getValue() + ByteUtils.UINT_128_LENGTH);
        return value;
    }

    private static PublicKey readPubkey(byte[] data, MutableInt offset) {
        PublicKey publicKey = PublicKey.readPubkey(data, offset.getValue());
        offset.setValue(offset.getValue() + PublicKey.PUBLIC_KEY_LENGTH);
        return publicKey;
    }

    private static List<BigInteger> readU64PaddingList(byte[] data, MutableInt offset) {
        int u64PaddingStartingOffset = offset.getValue();
        List<BigInteger> u64PaddingList = new ArrayList<>();
        while (u64PaddingStartingOffset < data.length) {
            u64PaddingList.add(ByteUtils.readUint64(data, u64PaddingStartingOffset));
            u64PaddingStartingOffset += 8;
        }
        return u64PaddingList;
    }

    // Wrapper class to hold an integer value
    @Setter
    @Getter
    @AllArgsConstructor
    private static class MutableInt {
        private int value;
    }
}
