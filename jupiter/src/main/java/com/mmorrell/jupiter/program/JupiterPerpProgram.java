package com.mmorrell.jupiter.program;

import com.mmorrell.jupiter.util.JupiterUtil;
import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.Program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * The JupiterPerpProgram class provides methods to interact with the Jupiter Perpetuals program on Solana.
 * It includes instructions for initializing the program, creating positions, managing perpetual contracts,
 * handling liquidity, withdrawing collateral, swapping tokens, and updating fee rates.
 */
@Slf4j
public class JupiterPerpProgram extends Program {

    /**
     * The Program ID for the Jupiter Perpetuals program.
     */
    public static final PublicKey JUPITER_PERP_PROGRAM_ID = new PublicKey("PERPHjGBqRHArX4DySjwM6UJHiR3sWAatqfdBS2qQJu");

    /**
     * Enum representing the different types of instructions for the Jupiter Perpetuals program.
     */
    public static enum InstructionType {
        INITIALIZE((byte) 0),
        CREATE_POSITION((byte) 1),
        CLOSE_POSITION((byte) 2),
        INCREASE_POSITION((byte) 3),
        DECREASE_POSITION((byte) 4),
        LIQUIDATE_POSITION((byte) 5),
        ADD_LIQUIDITY((byte) 6),
        REMOVE_LIQUIDITY((byte) 7),
        WITHDRAW_COLLATERAL((byte) 8),
        SWAP((byte) 9),
        UPDATE_FEE_RATE((byte) 10),
        // Add more instruction types as defined in perpetuals.json
        // For example:
        UPDATE_MAX_LEVERAGE((byte) 11),
        // ...
        ;

        private final byte value;

        InstructionType(byte value) {
            this.value = value;
        }

        /**
         * Gets the byte value associated with the instruction type.
         *
         * @return The byte value.
         */
        public byte getValue() {
            return value;
        }
    }

    /**
     * Represents the configuration parameters for initializing the Jupiter Perpetuals program.
     */
    public static class InitializeConfig {
        public static final int BYTES = 16; // 8 bytes for maxLeverage, 8 bytes for feeRate

        private final long maxLeverage;
        private final long feeRate;

        /**
         * Constructs a new InitializeConfig.
         *
         * @param maxLeverage The maximum leverage allowed.
         * @param feeRate     The fee rate applied.
         */
        public InitializeConfig(long maxLeverage, long feeRate) {
            this.maxLeverage = maxLeverage;
            this.feeRate = feeRate;
        }

        /**
         * Serializes the InitializeConfig to a byte array.
         *
         * @return The serialized byte array.
         */
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(BYTES).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(maxLeverage);
            buffer.putLong(feeRate);
            return buffer.array();
        }

        public long getMaxLeverage() {
            return maxLeverage;
        }

        public long getFeeRate() {
            return feeRate;
        }
    }

    /**
     * Represents the configuration parameters for creating a new perpetual position.
     */
    public static class CreatePositionConfig {
        public static final int BYTES = 24; // 8 bytes for leverage, 8 bytes for positionSize, 8 bytes for collateralAmount

        private final double leverage;
        private final long positionSize;
        private final long collateralAmount;

        /**
         * Constructs a new CreatePositionConfig.
         *
         * @param leverage          The leverage for the position.
         * @param positionSize      The size of the position.
         * @param collateralAmount  The amount of collateral.
         */
        public CreatePositionConfig(double leverage, long positionSize, long collateralAmount) {
            this.leverage = leverage;
            this.positionSize = positionSize;
            this.collateralAmount = collateralAmount;
        }

        /**
         * Serializes the CreatePositionConfig to a byte array.
         *
         * @return The serialized byte array.
         */
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(BYTES).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putDouble(leverage);
            buffer.putLong(positionSize);
            buffer.putLong(collateralAmount);
            return buffer.array();
        }

        public double getLeverage() {
            return leverage;
        }

        public long getPositionSize() {
            return positionSize;
        }

        public long getCollateralAmount() {
            return collateralAmount;
        }
    }

    /**
     * Represents the configuration parameters for increasing a perpetual position.
     */
    public static class IncreasePositionConfig {
        public static final int BYTES = 16; // 8 bytes for additionalSize, 8 bytes for additionalCollateral

        private final long additionalSize;
        private final long additionalCollateral;

        /**
         * Constructs a new IncreasePositionConfig.
         *
         * @param additionalSize        The additional size to increase.
         * @param additionalCollateral  The additional collateral to add.
         */
        public IncreasePositionConfig(long additionalSize, long additionalCollateral) {
            this.additionalSize = additionalSize;
            this.additionalCollateral = additionalCollateral;
        }

        /**
         * Serializes the IncreasePositionConfig to a byte array.
         *
         * @return The serialized byte array.
         */
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(BYTES).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(additionalSize);
            buffer.putLong(additionalCollateral);
            return buffer.array();
        }

        public long getAdditionalSize() {
            return additionalSize;
        }

        public long getAdditionalCollateral() {
            return additionalCollateral;
        }
    }

    /**
     * Represents the configuration parameters for decreasing a perpetual position.
     */
    public static class DecreasePositionConfig {
        public static final int BYTES = 16; // 8 bytes for reductionSize, 8 bytes for collateralReturn

        private final long reductionSize;
        private final long collateralReturn;

        /**
         * Constructs a new DecreasePositionConfig.
         *
         * @param reductionSize     The size to reduce.
         * @param collateralReturn  The collateral to return.
         */
        public DecreasePositionConfig(long reductionSize, long collateralReturn) {
            this.reductionSize = reductionSize;
            this.collateralReturn = collateralReturn;
        }

        /**
         * Serializes the DecreasePositionConfig to a byte array.
         *
         * @return The serialized byte array.
         */
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(BYTES).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(reductionSize);
            buffer.putLong(collateralReturn);
            return buffer.array();
        }

        public long getReductionSize() {
            return reductionSize;
        }

        public long getCollateralReturn() {
            return collateralReturn;
        }
    }

    /**
     * Represents the configuration parameters for liquidating a perpetual position.
     */
    public static class LiquidatePositionConfig {
        public static final int BYTES = 8; // Example size, adjust as needed

        private final long liquidationAmount;

        /**
         * Constructs a new LiquidatePositionConfig.
         *
         * @param liquidationAmount The amount to liquidate.
         */
        public LiquidatePositionConfig(long liquidationAmount) {
            this.liquidationAmount = liquidationAmount;
        }

        /**
         * Serializes the LiquidatePositionConfig to a byte array.
         *
         * @return The serialized byte array.
         */
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(BYTES).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(liquidationAmount);
            return buffer.array();
        }

        public long getLiquidationAmount() {
            return liquidationAmount;
        }
    }

    /**
     * Represents the configuration parameters for adding liquidity.
     */
    public static class AddLiquidityConfig {
        public static final int BYTES = 16; // 8 bytes for amount, 8 bytes for minLpTokens

        private final long amount;
        private final long minLpTokens;

        /**
         * Constructs a new AddLiquidityConfig.
         *
         * @param amount        The amount of tokens to add.
         * @param minLpTokens   The minimum LP tokens to receive.
         */
        public AddLiquidityConfig(long amount, long minLpTokens) {
            this.amount = amount;
            this.minLpTokens = minLpTokens;
        }

        /**
         * Serializes the AddLiquidityConfig to a byte array.
         *
         * @return The serialized byte array.
         */
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(BYTES).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(amount);
            buffer.putLong(minLpTokens);
            return buffer.array();
        }

        public long getAmount() {
            return amount;
        }

        public long getMinLpTokens() {
            return minLpTokens;
        }
    }

    /**
     * Represents the configuration parameters for removing liquidity.
     */
    public static class RemoveLiquidityConfig {
        public static final int BYTES = 16; // 8 bytes for lpTokens, 8 bytes for minAmount

        private final long lpTokens;
        private final long minAmount;

        /**
         * Constructs a new RemoveLiquidityConfig.
         *
         * @param lpTokens  The amount of LP tokens to remove.
         * @param minAmount The minimum amount of tokens to receive.
         */
        public RemoveLiquidityConfig(long lpTokens, long minAmount) {
            this.lpTokens = lpTokens;
            this.minAmount = minAmount;
        }

        /**
         * Serializes the RemoveLiquidityConfig to a byte array.
         *
         * @return The serialized byte array.
         */
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(BYTES).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(lpTokens);
            buffer.putLong(minAmount);
            return buffer.array();
        }

        public long getLpTokens() {
            return lpTokens;
        }

        public long getMinAmount() {
            return minAmount;
        }
    }

    /**
     * Represents the configuration parameters for withdrawing collateral.
     */
    public static class WithdrawCollateralConfig {
        public static final int BYTES = 16; // 8 bytes for amount, 8 bytes for minWithdrawal

        private final long amount;
        private final long minWithdrawal;

        /**
         * Constructs a new WithdrawCollateralConfig.
         *
         * @param amount         The amount of collateral to withdraw.
         * @param minWithdrawal  The minimum amount to receive.
         */
        public WithdrawCollateralConfig(long amount, long minWithdrawal) {
            this.amount = amount;
            this.minWithdrawal = minWithdrawal;
        }

        /**
         * Serializes the WithdrawCollateralConfig to a byte array.
         *
         * @return The serialized byte array.
         */
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(BYTES).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(amount);
            buffer.putLong(minWithdrawal);
            return buffer.array();
        }

        public long getAmount() {
            return amount;
        }

        public long getMinWithdrawal() {
            return minWithdrawal;
        }
    }

    /**
     * Represents the configuration parameters for swapping tokens.
     */
    public static class SwapConfig {
        public static final int BYTES = 16; // 8 bytes for amountIn, 8 bytes for minAmountOut

        private final long amountIn;
        private final long minAmountOut;

        /**
         * Constructs a new SwapConfig.
         *
         * @param amountIn      The amount of tokens to swap.
         * @param minAmountOut  The minimum amount of tokens to receive.
         */
        public SwapConfig(long amountIn, long minAmountOut) {
            this.amountIn = amountIn;
            this.minAmountOut = minAmountOut;
        }

        /**
         * Serializes the SwapConfig to a byte array.
         *
         * @return The serialized byte array.
         */
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(BYTES).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(amountIn);
            buffer.putLong(minAmountOut);
            return buffer.array();
        }

        public long getAmountIn() {
            return amountIn;
        }

        public long getMinAmountOut() {
            return minAmountOut;
        }
    }

    /**
     * Represents the configuration parameters for updating the fee rate.
     */
    public static class UpdateFeeRateConfig {
        public static final int BYTES = 8; // 8 bytes for newFeeRate

        private final long newFeeRate;

        /**
         * Constructs a new UpdateFeeRateConfig.
         *
         * @param newFeeRate The new fee rate to set.
         */
        public UpdateFeeRateConfig(long newFeeRate) {
            this.newFeeRate = newFeeRate;
        }

        /**
         * Serializes the UpdateFeeRateConfig to a byte array.
         *
         * @return The serialized byte array.
         */
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(BYTES).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(newFeeRate);
            return buffer.array();
        }

        public long getNewFeeRate() {
            return newFeeRate;
        }
    }

    /**
     * Represents the configuration parameters for updating the maximum leverage.
     */
    public static class UpdateMaxLeverageConfig {
        public static final int BYTES = 8; // 8 bytes for maxLeverage

        private final long maxLeverage;

        /**
         * Constructs a new UpdateMaxLeverageConfig.
         *
         * @param maxLeverage The new maximum leverage to set.
         */
        public UpdateMaxLeverageConfig(long maxLeverage) {
            this.maxLeverage = maxLeverage;
        }

        /**
         * Serializes the UpdateMaxLeverageConfig to a byte array.
         *
         * @return The serialized byte array.
         */
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(BYTES).order(ByteOrder.LITTLE_ENDIAN);
            buffer.putLong(maxLeverage);
            return buffer.array();
        }

        public long getMaxLeverage() {
            return maxLeverage;
        }
    }

    /**
     * Creates a transaction instruction to initialize the Jupiter Perpetuals program.
     *
     * @param initializer The public key of the initializer account.
     * @param config      The configuration parameters for initialization.
     * @return A TransactionInstruction object.
     */
    public TransactionInstruction initialize(PublicKey initializer, InitializeConfig config) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(initializer, true, false));
        keys.add(new AccountMeta(JUPITER_PERP_PROGRAM_ID, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1 + InitializeConfig.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(InstructionType.INITIALIZE.getValue());
        buffer.put(config.toBytes());

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates a transaction instruction to create a new perpetual position.
     *
     * @param user           The public key of the user creating the position.
     * @param position       The public key of the position account.
     * @param collateral     The public key of the collateral account.
     * @param collateralMint The public key of the collateral mint.
     * @param config         The configuration parameters for the position.
     * @return A TransactionInstruction object.
     */
    public TransactionInstruction createPosition(PublicKey user, PublicKey position, PublicKey collateral,
                                                 PublicKey collateralMint, CreatePositionConfig config) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, false));
        keys.add(new AccountMeta(position, false, true));
        keys.add(new AccountMeta(collateral, false, true));
        keys.add(new AccountMeta(collateralMint, false, false));
        keys.add(new AccountMeta(JUPITER_PERP_PROGRAM_ID, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1 + CreatePositionConfig.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(InstructionType.CREATE_POSITION.getValue());
        buffer.put(config.toBytes());

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates a transaction instruction to close an existing perpetual position.
     *
     * @param user     The public key of the user closing the position.
     * @param position The public key of the position account.
     * @return A TransactionInstruction object.
     */
    public TransactionInstruction closePosition(PublicKey user, PublicKey position) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, false));
        keys.add(new AccountMeta(position, false, true));
        keys.add(new AccountMeta(JUPITER_PERP_PROGRAM_ID, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(InstructionType.CLOSE_POSITION.getValue());

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates a transaction instruction to increase an existing perpetual position.
     *
     * @param user     The public key of the user.
     * @param position The public key of the position account.
     * @param config   The configuration parameters for increasing the position.
     * @return A TransactionInstruction object.
     */
    public TransactionInstruction increasePosition(PublicKey user, PublicKey position, IncreasePositionConfig config) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, false));
        keys.add(new AccountMeta(position, false, true));
        keys.add(new AccountMeta(JUPITER_PERP_PROGRAM_ID, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1 + IncreasePositionConfig.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(InstructionType.INCREASE_POSITION.getValue());
        buffer.put(config.toBytes());

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates a transaction instruction to decrease an existing perpetual position.
     *
     * @param user     The public key of the user.
     * @param position The public key of the position account.
     * @param config   The configuration parameters for decreasing the position.
     * @return A TransactionInstruction object.
     */
    public TransactionInstruction decreasePosition(PublicKey user, PublicKey position, DecreasePositionConfig config) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, false));
        keys.add(new AccountMeta(position, false, true));
        keys.add(new AccountMeta(JUPITER_PERP_PROGRAM_ID, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1 + DecreasePositionConfig.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(InstructionType.DECREASE_POSITION.getValue());
        buffer.put(config.toBytes());

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates a transaction instruction to liquidate a perpetual position.
     *
     * @param liquidator The public key of the liquidator.
     * @param position   The public key of the position account to be liquidated.
     * @param config     The configuration parameters for liquidation.
     * @return A TransactionInstruction object.
     */
    public TransactionInstruction liquidatePosition(PublicKey liquidator, PublicKey position, LiquidatePositionConfig config) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(liquidator, true, false));
        keys.add(new AccountMeta(position, false, true));
        keys.add(new AccountMeta(JUPITER_PERP_PROGRAM_ID, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1 + LiquidatePositionConfig.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(InstructionType.LIQUIDATE_POSITION.getValue());
        buffer.put(config.toBytes());

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates a transaction instruction to add liquidity to the pool.
     *
     * @param user      The public key of the user adding liquidity.
     * @param pool      The public key of the pool.
     * @param liquidity The public key of the liquidity account.
     * @param config    The configuration parameters for adding liquidity.
     * @return A TransactionInstruction object.
     */
    public TransactionInstruction addLiquidity(PublicKey user, PublicKey pool, PublicKey liquidity, AddLiquidityConfig config) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, false));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(liquidity, false, true));
        keys.add(new AccountMeta(JUPITER_PERP_PROGRAM_ID, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1 + AddLiquidityConfig.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(InstructionType.ADD_LIQUIDITY.getValue());
        buffer.put(config.toBytes());

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates a transaction instruction to remove liquidity from the pool.
     *
     * @param user      The public key of the user removing liquidity.
     * @param pool      The public key of the pool.
     * @param liquidity The public key of the liquidity account.
     * @param config    The configuration parameters for removing liquidity.
     * @return A TransactionInstruction object.
     */
    public TransactionInstruction removeLiquidity(PublicKey user, PublicKey pool, PublicKey liquidity, RemoveLiquidityConfig config) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, false));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(liquidity, false, true));
        keys.add(new AccountMeta(JUPITER_PERP_PROGRAM_ID, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1 + RemoveLiquidityConfig.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(InstructionType.REMOVE_LIQUIDITY.getValue());
        buffer.put(config.toBytes());

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates a transaction instruction to withdraw collateral from a position.
     *
     * @param user         The public key of the user withdrawing collateral.
     * @param position     The public key of the position account.
     * @param collateral   The public key of the collateral account.
     * @param config       The configuration parameters for withdrawing collateral.
     * @return A TransactionInstruction object.
     */
    public TransactionInstruction withdrawCollateral(PublicKey user, PublicKey position, PublicKey collateral, WithdrawCollateralConfig config) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, false));
        keys.add(new AccountMeta(position, false, true));
        keys.add(new AccountMeta(collateral, false, true));
        keys.add(new AccountMeta(JUPITER_PERP_PROGRAM_ID, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1 + WithdrawCollateralConfig.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(InstructionType.WITHDRAW_COLLATERAL.getValue());
        buffer.put(config.toBytes());

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates a transaction instruction to swap tokens within the program.
     *
     * @param user         The public key of the user performing the swap.
     * @param pool         The public key of the pool.
     * @param source       The public key of the source token account.
     * @param destination  The public key of the destination token account.
     * @param config       The configuration parameters for swapping.
     * @return A TransactionInstruction object.
     */
    public TransactionInstruction swap(PublicKey user, PublicKey pool, PublicKey source, PublicKey destination, SwapConfig config) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, false));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(source, false, true));
        keys.add(new AccountMeta(destination, false, true));
        keys.add(new AccountMeta(JUPITER_PERP_PROGRAM_ID, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1 + SwapConfig.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(InstructionType.SWAP.getValue());
        buffer.put(config.toBytes());

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates a transaction instruction to update the fee rate of the Jupiter Perpetuals program.
     *
     * @param initializer The public key of the initializer account.
     * @param config      The configuration parameters for updating the fee rate.
     * @return A TransactionInstruction object.
     */
    public TransactionInstruction updateFeeRate(PublicKey initializer, UpdateFeeRateConfig config) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(initializer, true, false));
        keys.add(new AccountMeta(JUPITER_PERP_PROGRAM_ID, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1 + UpdateFeeRateConfig.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(InstructionType.UPDATE_FEE_RATE.getValue());
        buffer.put(config.toBytes());

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates a transaction instruction to update the maximum leverage of the Jupiter Perpetuals program.
     *
     * @param initializer The public key of the initializer account.
     * @param config      The configuration parameters for updating the maximum leverage.
     * @return A TransactionInstruction object.
     */
    public TransactionInstruction updateMaxLeverage(PublicKey initializer, UpdateMaxLeverageConfig config) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(initializer, true, false));
        keys.add(new AccountMeta(JUPITER_PERP_PROGRAM_ID, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1 + UpdateMaxLeverageConfig.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(InstructionType.UPDATE_MAX_LEVERAGE.getValue());
        buffer.put(config.toBytes());

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    // Implement additional instruction methods here following the same pattern.

}