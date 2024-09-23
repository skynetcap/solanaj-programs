package com.mmorrell.jupiter.model;

import com.mmorrell.jupiter.util.JupiterUtil;
import com.mmorrell.openbook.OpenBookUtil;

import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;

/**
 * Represents a Jupiter Custody account in Jupiter Perpetuals.
 */
@Data
@Builder
public class JupiterCustody {
    private PublicKey pool;
    private PublicKey mint;
    private PublicKey tokenAccount;
    private byte decimals;
    private boolean isStable;
    private OracleParams oracle;
    private PricingParams pricing;
    private Permissions permissions;
    private long targetRatioBps;
    private Assets assets;
    private FundingRateState fundingRateState;
    private byte bump;
    private byte tokenAccountBump;

    @Data
    @Builder
    public static class OracleParams {
        private PublicKey oracleAccount;
        private byte oracleType;
        private long maxPriceError;
        private int maxPriceAgeSec;
    }

    @Data
    @Builder
    public static class PricingParams {
        private long tradeImpactFeeScalar;
        private long buffer;
        private long swapSpread;
        private long maxLeverage;
        private long maxGlobalLongSizes;
        private long maxGlobalShortSizes;
    }

    @Data
    @Builder
    public static class Permissions {
        private boolean allowDeposit;
        private boolean allowWithdraw;
        private boolean allowTrade;
        private boolean allowSwap;
        private boolean allowAddLiquidity;
        private boolean allowRemoveLiquidity;
        private boolean allowUseAsCollateral;
    }

    @Data
    @Builder
    public static class Assets {
        private long feesReserves;
        private long owned;
        private long locked;
        private long guaranteedUsd;
        private long globalShortSizes;
        private long globalShortAveragePrices;
    }

    @Data
    @Builder
    public static class FundingRateState {
        private long cumulativeInterestRate;
        private long lastUpdate;
        private long hourlyFundingDbps;
    }

    /**
     * Deserializes a byte array into a JupiterCustody object.
     *
     * @param data the byte array representing the account data.
     * @return a JupiterCustody object.
     */
    public static JupiterCustody fromByteArray(byte[] data) {
        int offset = 8; // Skip discriminator

        PublicKey pool = PublicKey.readPubkey(data, offset);
        offset += 32;

        PublicKey mint = PublicKey.readPubkey(data, offset);
        offset += 32;

        PublicKey tokenAccount = PublicKey.readPubkey(data, offset);
        offset += 32;

        byte decimals = data[offset++];
        boolean isStable = data[offset++] != 0;

        OracleParams oracle = readOracleParams(data, offset);
        offset += 45; // 32 (publicKey) + 1 (oracleType) + 8 (maxPriceError) + 4 (maxPriceAgeSec)

        PricingParams pricing = readPricingParams(data, offset);
        offset += 48; // Adjust based on actual size

        Permissions permissions = readPermissions(data, offset);
        offset += 7; // Adjust based on actual size

        long targetRatioBps = JupiterUtil.readUint64(data, offset);
        offset += 8;

        Assets assets = readAssets(data, offset);
        offset += 48; // 6 fields * 8 bytes each

        FundingRateState fundingRateState = readFundingRateState(data, offset);
        offset += 32; // 16 (cumulativeInterestRate) + 8 (lastUpdate) + 8 (hourlyFundingDbps)

        byte bump = data[offset++];
        byte tokenAccountBump = data[offset];

        return JupiterCustody.builder()
                .pool(pool)
                .mint(mint)
                .tokenAccount(tokenAccount)
                .decimals(decimals)
                .isStable(isStable)
                .oracle(oracle)
                .pricing(pricing)
                .permissions(permissions)
                .targetRatioBps(targetRatioBps)
                .assets(assets)
                .fundingRateState(fundingRateState)
                .bump(bump)
                .tokenAccountBump(tokenAccountBump)
                .build();
    }

    private static OracleParams readOracleParams(byte[] data, int offset) {
        PublicKey oracleAccount = PublicKey.readPubkey(data, offset);
        offset += 32;
        byte oracleType = data[offset++];
        long maxPriceError = JupiterUtil.readUint64(data, offset);
        offset += 8;
        int maxPriceAgeSec = OpenBookUtil.readInt32(data, offset);

        return OracleParams.builder()
                .oracleAccount(oracleAccount)
                .oracleType(oracleType)
                .maxPriceError(maxPriceError)
                .maxPriceAgeSec(maxPriceAgeSec)
                .build();
    }

    private static PricingParams readPricingParams(byte[] data, int offset) {
        long tradeImpactFeeScalar = JupiterUtil.readUint64(data, offset);
        offset += 8;
        long buffer = JupiterUtil.readUint64(data, offset);
        offset += 8;
        long swapSpread = JupiterUtil.readUint64(data, offset);
        offset += 8;
        long maxLeverage = JupiterUtil.readUint64(data, offset);
        offset += 8;
        long maxGlobalLongSizes = JupiterUtil.readUint64(data, offset);
        offset += 8;
        long maxGlobalShortSizes = JupiterUtil.readUint64(data, offset);

        return PricingParams.builder()
                .tradeImpactFeeScalar(tradeImpactFeeScalar)
                .buffer(buffer)
                .swapSpread(swapSpread)
                .maxLeverage(maxLeverage)
                .maxGlobalLongSizes(maxGlobalLongSizes)
                .maxGlobalShortSizes(maxGlobalShortSizes)
                .build();
    }

    private static Permissions readPermissions(byte[] data, int offset) {
        return Permissions.builder()
                .allowDeposit(data[offset++] != 0)
                .allowWithdraw(data[offset++] != 0)
                .allowTrade(data[offset++] != 0)
                .allowSwap(data[offset++] != 0)
                .allowAddLiquidity(data[offset++] != 0)
                .allowRemoveLiquidity(data[offset++] != 0)
                .allowUseAsCollateral(data[offset] != 0)
                .build();
    }

    private static Assets readAssets(byte[] data, int offset) {
        long feesReserves = JupiterUtil.readUint64(data, offset);
        offset += 8;
        long owned = JupiterUtil.readUint64(data, offset);
        offset += 8;
        long locked = JupiterUtil.readUint64(data, offset);
        offset += 8;
        long guaranteedUsd = JupiterUtil.readUint64(data, offset);
        offset += 8;
        long globalShortSizes = JupiterUtil.readUint64(data, offset);
        offset += 8;
        long globalShortAveragePrices = JupiterUtil.readUint64(data, offset);

        return Assets.builder()
                .feesReserves(feesReserves)
                .owned(owned)
                .locked(locked)
                .guaranteedUsd(guaranteedUsd)
                .globalShortSizes(globalShortSizes)
                .globalShortAveragePrices(globalShortAveragePrices)
                .build();
    }

    private static FundingRateState readFundingRateState(byte[] data, int offset) {
        long cumulativeInterestRate = OpenBookUtil.readUint128(data, offset).longValue();
        offset += 16;
        long lastUpdate = JupiterUtil.readInt64(data, offset);
        offset += 8;
        long hourlyFundingDbps = JupiterUtil.readUint64(data, offset);

        return FundingRateState.builder()
                .cumulativeInterestRate(cumulativeInterestRate)
                .lastUpdate(lastUpdate)
                .hourlyFundingDbps(hourlyFundingDbps)
                .build();
    }
}