package com.mmorrell.jupiter.program;

import com.mmorrell.jupiter.util.JupiterUtil;
import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.Program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * The JupiterPerpProgram class provides methods to interact with the Jupiter Perpetuals program on Solana.
 * It includes instructions for initializing the program, adding pools, managing custody, and more.
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
        init,
        addPool,
        addCustody,
        setCustodyConfig,
        setCustodyGlobalLimit,
        setPoolConfig,
        setPerpetualsConfig,
        transferAdmin,
        withdrawFees,
        withdrawFees2,
        createTokenMetadata,
        testInit,
        setTestOraclePrice,
        setTestTime,
        swap,
        swap2,
        swapExactOut,
        addLiquidity,
        addLiquidity2,
        removeLiquidity,
        removeLiquidity2,
        createIncreasePositionRequest,
        createIncreasePositionMarketRequest,
        updateIncreasePositionRequest,
        createDecreasePositionRequest,
        createDecreasePositionRequest2,
        createDecreasePositionMarketRequest,
        updateDecreasePositionRequest,
        updateDecreasePositionRequest2,
        closePositionRequest,
        increasePosition2,
        increasePosition4,
        increasePositionPreSwap,
        decreasePosition2,
        decreasePositionPostSwap,
        decreasePosition3,
        decreasePosition4,
        liquidateFullPosition2,
        liquidateFullPosition4,
        refreshAssetsUnderManagement,
        instantCreateTpsl,
        instantUpdateTpsl,
        getAddLiquidityAmountAndFee,
        getAddLiquidityAmountAndFee2,
        getRemoveLiquidityAmountAndFee,
        getRemoveLiquidityAmountAndFee2,
        getIncreasePosition,
        getDecreasePosition,
        getPnl,
        getLiquidationState,
        getOraclePrice,
        getSwapAmountAndFees,
        getExactOutSwapAmountAndFees,
        getAssetsUnderManagement,
        getAssetsUnderManagement2;
    }

    /**
     * Creates an instruction to initialize the Jupiter Perpetuals program.
     *
     * @param admin The public key of the admin account.
     * @param perpetuals The public key of the perpetuals account.
     * @param pool The public key of the pool account.
     * @param transferAuthority The public key of the transfer authority account.
     * @return A TransactionInstruction object for initializing the program.
     */
    public static TransactionInstruction init(
            PublicKey admin,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey transferAuthority
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, false));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(transferAuthority, false, false));

        // Use the existing method to get the instruction discriminator
        byte[] instructionData = getInstructionDiscriminator(InstructionType.init.name());

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                instructionData
        );
    }

    /**
     * Creates an instruction to add a pool to the Jupiter Perpetuals program.
     *
     * @param admin The public key of the admin account.
     * @param perpetuals The public key of the perpetuals account.
     * @param pool The public key of the pool account to be added.
     * @param lpTokenMint The public key of the LP token mint account.
     * @param name The name of the pool.
     * @return A TransactionInstruction object for adding a pool.
     */
    public static TransactionInstruction addPoolInstruction(
            PublicKey admin,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey lpTokenMint,
            String name
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, false));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(lpTokenMint, false, true));

        ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + name.getBytes().length);
        buffer.put((byte) InstructionType.addPool.ordinal());
        buffer.putInt(name.getBytes().length);
        buffer.put(name.getBytes());

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates an instruction to add a custody to the Jupiter Perpetuals program.
     *
     * @param admin The public key of the admin account.
     * @param perpetuals The public key of the perpetuals account.
     * @param pool The public key of the pool account.
     * @param custody The public key of the custody account to be added.
     * @param mint The public key of the token mint account.
     * @param tokenAccount The public key of the token account.
     * @param oracleAccount The public key of the oracle account.
     * @param oracleType The type of oracle being used.
     * @return A TransactionInstruction object for adding a custody.
     */
    public static TransactionInstruction addCustodyInstruction(
            PublicKey admin,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey custody,
            PublicKey mint,
            PublicKey tokenAccount,
            PublicKey oracleAccount,
            byte oracleType
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, false));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(custody, false, true));
        keys.add(new AccountMeta(mint, false, false));
        keys.add(new AccountMeta(tokenAccount, false, true));
        keys.add(new AccountMeta(oracleAccount, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) InstructionType.addCustody.ordinal());
        buffer.put(oracleType);

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates an instruction to set the custody configuration for the Jupiter Perpetuals program.
     *
     * @param admin The public key of the admin account.
     * @param perpetuals The public key of the perpetuals account.
     * @param pool The public key of the pool account.
     * @param custody The public key of the custody account.
     * @param config The custody configuration data.
     * @return A TransactionInstruction object for setting the custody configuration.
     */
    public static TransactionInstruction setCustodyConfigInstruction(
            PublicKey admin,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey custody,
            byte[] config
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, false));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(custody, false, true));

        ByteBuffer buffer = ByteBuffer.allocate(1 + config.length);
        buffer.put((byte) InstructionType.setCustodyConfig.ordinal());
        buffer.put(config);

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates an instruction to set the global custody limit for the Jupiter Perpetuals program.
     *
     * @param admin The public key of the admin account.
     * @param perpetuals The public key of the perpetuals account.
     * @param pool The public key of the pool account.
     * @param custody The public key of the custody account.
     * @param limit The global custody limit.
     * @return A TransactionInstruction object for setting the global custody limit.
     */
    public static TransactionInstruction setCustodyGlobalLimitInstruction(
            PublicKey admin,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey custody,
            long limit
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, false));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(custody, false, true));

        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.put((byte) InstructionType.setCustodyGlobalLimit.ordinal());
        buffer.putLong(limit);

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates an instruction to set the pool configuration for the Jupiter Perpetuals program.
     *
     * @param admin The public key of the admin account.
     * @param perpetuals The public key of the perpetuals account.
     * @param pool The public key of the pool account.
     * @param config The pool configuration data.
     * @return A TransactionInstruction object for setting the pool configuration.
     */
    public static TransactionInstruction setPoolConfigInstruction(
            PublicKey admin,
            PublicKey perpetuals,
            PublicKey pool,
            byte[] config
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, false));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));

        ByteBuffer buffer = ByteBuffer.allocate(1 + config.length);
        buffer.put((byte) InstructionType.setPoolConfig.ordinal());
        buffer.put(config);

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates an instruction to set the perpetuals configuration for the Jupiter Perpetuals program.
     *
     * @param admin The public key of the admin account.
     * @param perpetuals The public key of the perpetuals account.
     * @param config The perpetuals configuration data.
     * @return A TransactionInstruction object for setting the perpetuals configuration.
     */
    public static TransactionInstruction setPerpetualsConfigInstruction(
            PublicKey admin,
            PublicKey perpetuals,
            byte[] config
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, false));
        keys.add(new AccountMeta(perpetuals, false, true));

        ByteBuffer buffer = ByteBuffer.allocate(1 + config.length);
        buffer.put((byte) InstructionType.setPerpetualsConfig.ordinal());
        buffer.put(config);

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates an instruction to transfer the admin authority for the Jupiter Perpetuals program.
     *
     * @param admin The public key of the current admin account.
     * @param newAdmin The public key of the new admin account.
     * @return A TransactionInstruction object for transferring the admin authority.
     */
    public static TransactionInstruction transferAdminInstruction(
            PublicKey admin,
            PublicKey newAdmin
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, false));
        keys.add(new AccountMeta(newAdmin, false, false));

        byte[] instructionData = getInstructionDiscriminator(InstructionType.transferAdmin.name());

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                instructionData
        );
    }

    /**
     * Creates an instruction to withdraw fees from the Jupiter Perpetuals program.
     *
     * @param admin The public key of the admin account.
     * @param perpetuals The public key of the perpetuals account.
     * @param pool The public key of the pool account.
     * @param custody The public key of the custody account.
     * @param feeReceiver The public key of the fee receiver account.
     * @param amount The amount of fees to withdraw.
     * @return A TransactionInstruction object for withdrawing fees.
     */
    public static TransactionInstruction withdrawFeesInstruction(
            PublicKey admin,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey custody,
            PublicKey feeReceiver,
            long amount
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, false));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(custody, false, true));
        keys.add(new AccountMeta(feeReceiver, false, true));
        keys.add(new AccountMeta(PublicKey.valueOf("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), false, false));

        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.put((byte) InstructionType.withdrawFees.ordinal());
        buffer.putLong(amount);

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    private static byte[] getInstructionDiscriminator(String instructionName) {
        try {
            String discriminatorString = "global:" + instructionName;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(discriminatorString.getBytes());
            byte[] discriminator = new byte[8];
            System.arraycopy(hash, 0, discriminator, 0, 8);
            return discriminator;
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found.", e);
            throw new RuntimeException(e);
        }
    }
}