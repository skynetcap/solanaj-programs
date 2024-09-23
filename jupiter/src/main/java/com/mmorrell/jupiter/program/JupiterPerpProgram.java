package com.mmorrell.jupiter.program;

import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.Program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
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

        // No additional parameters to serialize
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
    public static TransactionInstruction addPool(
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

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.addPool.name());

        // Serialize 'name' parameter using Borsh
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        int nameLength = nameBytes.length;

        ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + nameLength); // 8 bytes for discriminator, 4 bytes for length, name bytes
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putInt(nameLength); // u32 length in little-endian
        buffer.put(nameBytes);

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
    public static TransactionInstruction addCustody(
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

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.addCustody.name());

        ByteBuffer buffer = ByteBuffer.allocate(8 + 1); // 8 bytes for discriminator, 1 byte for oracleType
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
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
    public static TransactionInstruction setCustodyConfig(
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

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.setCustodyConfig.name());

        int configLength = config.length;

        ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + configLength); // 8 bytes for discriminator, 4 bytes for length, config bytes
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putInt(configLength); // u32 length
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
    public static TransactionInstruction setCustodyGlobalLimit(
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

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.setCustodyGlobalLimit.name());

        ByteBuffer buffer = ByteBuffer.allocate(8 + 8); // 8 bytes for discriminator, 8 bytes for limit
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putLong(limit); // u64, little-endian

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
    public static TransactionInstruction setPoolConfig(
            PublicKey admin,
            PublicKey perpetuals,
            PublicKey pool,
            byte[] config
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, false));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.setPoolConfig.name());

        int configLength = config.length;

        ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + configLength); // 8 bytes for discriminator, 4 bytes for length, config bytes
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putInt(configLength); // u32 length
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
    public static TransactionInstruction setPerpetualsConfig(
            PublicKey admin,
            PublicKey perpetuals,
            byte[] config
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, false));
        keys.add(new AccountMeta(perpetuals, false, true));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.setPerpetualsConfig.name());

        int configLength = config.length;

        ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + configLength); // 8 bytes for discriminator, 4 bytes for length, config bytes
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putInt(configLength); // u32 length
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
    public static TransactionInstruction transferAdmin(
            PublicKey admin,
            PublicKey newAdmin
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, false));
        keys.add(new AccountMeta(newAdmin, false, false));

        byte[] instructionData = getInstructionDiscriminator(InstructionType.transferAdmin.name());

        // No additional parameters to serialize

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
    public static TransactionInstruction withdrawFees(
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
        keys.add(new AccountMeta(new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), false, false));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.withdrawFees.name());

        ByteBuffer buffer = ByteBuffer.allocate(8 + 8); // 8 bytes for discriminator, 8 bytes for amount
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putLong(amount); // u64, little-endian

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates an instruction to withdraw fees using the version 2 of the withdrawFees instruction.
     *
     * @param admin        The public key of the admin account.
     * @param perpetuals   The public key of the perpetuals account.
     * @param pool         The public key of the pool account.
     * @param custody      The public key of the custody account.
     * @param feeReceiver  The public key of the fee receiver account.
     * @param receiverCustody The public key of the receiver custody account.
     * @param amount       The amount of fees to withdraw.
     * @return A TransactionInstruction object for withdrawing fees.
     */
    public static TransactionInstruction withdrawFees2(
            PublicKey admin,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey custody,
            PublicKey feeReceiver,
            PublicKey receiverCustody,
            long amount
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, false));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(custody, false, true));
        keys.add(new AccountMeta(feeReceiver, false, true));
        keys.add(new AccountMeta(receiverCustody, false, true));
        keys.add(new AccountMeta(new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), false, false));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.withdrawFees2.name());

        ByteBuffer buffer = ByteBuffer.allocate(8 + 8); // 8 bytes for discriminator, 8 bytes for amount
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putLong(amount); // u64, little-endian

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates an instruction to create token metadata.
     *
     * @param payer               The public key of the payer account.
     * @param lpTokenMint         The public key of the LP token mint account.
     * @param tokenMetadata       The public key of the token metadata account.
     * @param tokenMetadataProgram The public key of the token metadata program.
     * @param name                The name of the token.
     * @param symbol              The symbol of the token.
     * @param uri                 The URI of the token metadata.
     * @return A TransactionInstruction object for creating token metadata.
     */
    public static TransactionInstruction createTokenMetadata(
            PublicKey payer,
            PublicKey lpTokenMint,
            PublicKey tokenMetadata,
            PublicKey tokenMetadataProgram,
            String name,
            String symbol,
            String uri
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(payer, true, true));
        keys.add(new AccountMeta(lpTokenMint, false, true));
        keys.add(new AccountMeta(tokenMetadata, false, true));
        keys.add(new AccountMeta(tokenMetadataProgram, false, false));
        keys.add(new AccountMeta(new PublicKey("SysvarRent111111111111111111111111111111111"), false, false));
        keys.add(new AccountMeta(new PublicKey("BPFLoaderUpgradeab1e11111111111111111111111"), false, false));
        keys.add(new AccountMeta(new PublicKey("11111111111111111111111111111111"), false, false));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.createTokenMetadata.name());

        // Serialize 'name', 'symbol', and 'uri' parameters using Borsh
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] symbolBytes = symbol.getBytes(StandardCharsets.UTF_8);
        byte[] uriBytes = uri.getBytes(StandardCharsets.UTF_8);

        int nameLength = nameBytes.length;
        int symbolLength = symbolBytes.length;
        int uriLength = uriBytes.length;

        ByteBuffer buffer = ByteBuffer.allocate(
                8 + // discriminator
                4 + nameLength + // name
                4 + symbolLength + // symbol
                4 + uriLength // uri
        );
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put(instructionDiscriminator);
        buffer.putInt(nameLength);
        buffer.put(nameBytes);
        buffer.putInt(symbolLength);
        buffer.put(symbolBytes);
        buffer.putInt(uriLength);
        buffer.put(uriBytes);

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates a test initialization instruction.
     *
     * @param admin       The public key of the admin account.
     * @param perpetuals  The public key of the perpetuals account.
     * @return A TransactionInstruction object for test initialization.
     */
    public static TransactionInstruction testInit(
            PublicKey admin,
            PublicKey perpetuals
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, true));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(new PublicKey("SysvarRent111111111111111111111111111111111"), false, false));
        keys.add(new AccountMeta(new PublicKey("11111111111111111111111111111111"), false, false));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.testInit.name());

        // No additional parameters to serialize

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                instructionDiscriminator
        );
    }

    /**
     * Creates an instruction to set the test oracle price.
     *
     * @param admin          The public key of the admin account.
     * @param oracleAccount  The public key of the oracle account.
     * @param price          The new price to set.
     * @return A TransactionInstruction object for setting the test oracle price.
     */
    public static TransactionInstruction setTestOraclePrice(
            PublicKey admin,
            PublicKey oracleAccount,
            long price
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, true));
        keys.add(new AccountMeta(oracleAccount, false, true));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.setTestOraclePrice.name());

        ByteBuffer buffer = ByteBuffer.allocate(8 + 8); // discriminator + price
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putLong(price); // u64

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates an instruction to set the test time.
     *
     * @param admin       The public key of the admin account.
     * @param perpetuals  The public key of the perpetuals account.
     * @param time        The new time to set.
     * @return A TransactionInstruction object for setting the test time.
     */
    public static TransactionInstruction setTestTime(
            PublicKey admin,
            PublicKey perpetuals,
            long time
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(admin, true, true));
        keys.add(new AccountMeta(perpetuals, false, true));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.setTestTime.name());

        ByteBuffer buffer = ByteBuffer.allocate(8 + 8); // discriminator + time
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putLong(time); // u64

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates a swap instruction.
     *
     * @param user                The public key of the user account.
     * @param userTokenAccountIn  The public key of the user's input token account.
     * @param userTokenAccountOut The public key of the user's output token account.
     * @param perpetuals          The public key of the perpetuals account.
     * @param pool                The public key of the pool account.
     * @param custodyIn           The public key of the input custody account.
     * @param custodyOut          The public key of the output custody account.
     * @param amountIn            The amount of input tokens.
     * @param minAmountOut        The minimum amount of output tokens expected.
     * @return A TransactionInstruction object for swapping tokens.
     */
    public static TransactionInstruction swap(
            PublicKey user,
            PublicKey userTokenAccountIn,
            PublicKey userTokenAccountOut,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey custodyIn,
            PublicKey custodyOut,
            long amountIn,
            long minAmountOut
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, false));
        keys.add(new AccountMeta(userTokenAccountIn, false, true));
        keys.add(new AccountMeta(userTokenAccountOut, false, true));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(custodyIn, false, true));
        keys.add(new AccountMeta(custodyOut, false, true));
        keys.add(new AccountMeta(new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), false, false));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.swap.name());

        ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 8); // discriminator + amountIn + minAmountOut
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putLong(amountIn); // u64
        buffer.putLong(minAmountOut); // u64

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates a swapExactOut instruction.
     *
     * @param user                 The public key of the user account.
     * @param userTokenAccountIn   The public key of the user's input token account.
     * @param userTokenAccountOut  The public key of the user's output token account.
     * @param perpetuals           The public key of the perpetuals account.
     * @param pool                 The public key of the pool account.
     * @param custodyIn            The public key of the input custody account.
     * @param custodyOut           The public key of the output custody account.
     * @param maxAmountIn          The maximum amount of input tokens.
     * @param amountOut            The exact amount of output tokens desired.
     * @return A TransactionInstruction object for swapping tokens.
     */
    public static TransactionInstruction swapExactOut(
            PublicKey user,
            PublicKey userTokenAccountIn,
            PublicKey userTokenAccountOut,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey custodyIn,
            PublicKey custodyOut,
            long maxAmountIn,
            long amountOut
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, false));
        keys.add(new AccountMeta(userTokenAccountIn, false, true));
        keys.add(new AccountMeta(userTokenAccountOut, false, true));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(custodyIn, false, true));
        keys.add(new AccountMeta(custodyOut, false, true));
        keys.add(new AccountMeta(new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), false, false));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.swapExactOut.name());

        ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 8); // discriminator + maxAmountIn + amountOut
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putLong(maxAmountIn); // u64
        buffer.putLong(amountOut); // u64

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates an instruction to add liquidity to the pool.
     *
     * @param user               The public key of the user account.
     * @param userTokenAccount   The public key of the user's token account.
     * @param userLpTokenAccount The public key of the user's LP token account.
     * @param perpetuals         The public key of the perpetuals account.
     * @param pool               The public key of the pool account.
     * @param custody            The public key of the custody account.
     * @param lpTokenMint        The public key of the LP token mint account.
     * @param amount             The amount of tokens to add.
     * @param minLpAmount        The minimum amount of LP tokens expected.
     * @return A TransactionInstruction object for adding liquidity.
     */
    public static TransactionInstruction addLiquidity(
            PublicKey user,
            PublicKey userTokenAccount,
            PublicKey userLpTokenAccount,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey custody,
            PublicKey lpTokenMint,
            long amount,
            long minLpAmount
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, false));
        keys.add(new AccountMeta(userTokenAccount, false, true));
        keys.add(new AccountMeta(userLpTokenAccount, false, true));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(custody, false, true));
        keys.add(new AccountMeta(lpTokenMint, false, true));
        keys.add(new AccountMeta(new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), false, false));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.addLiquidity.name());

        ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 8); // discriminator + amount + minLpAmount
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putLong(amount); // u64
        buffer.putLong(minLpAmount); // u64

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates an instruction to remove liquidity from the pool.
     *
     * @param user               The public key of the user account.
     * @param userTokenAccount   The public key of the user's token account.
     * @param userLpTokenAccount The public key of the user's LP token account.
     * @param perpetuals         The public key of the perpetuals account.
     * @param pool               The public key of the pool account.
     * @param custody            The public key of the custody account.
     * @param lpTokenMint        The public key of the LP token mint account.
     * @param lpAmount           The amount of LP tokens to remove.
     * @param minAmountOut       The minimum amount of tokens expected.
     * @return A TransactionInstruction object for removing liquidity.
     */
    public static TransactionInstruction removeLiquidity(
            PublicKey user,
            PublicKey userTokenAccount,
            PublicKey userLpTokenAccount,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey custody,
            PublicKey lpTokenMint,
            long lpAmount,
            long minAmountOut
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, false));
        keys.add(new AccountMeta(userTokenAccount, false, true));
        keys.add(new AccountMeta(userLpTokenAccount, false, true));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(custody, false, true));
        keys.add(new AccountMeta(lpTokenMint, false, true));
        keys.add(new AccountMeta(new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), false, false));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.removeLiquidity.name());

        ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 8); // discriminator + lpAmount + minAmountOut
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putLong(lpAmount); // u64
        buffer.putLong(minAmountOut); // u64

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates an instruction to swap tokens using version 2 of the swap instruction.
     *
     * @param user                The public key of the user account.
     * @param userTokenAccountIn  The public key of the user's input token account.
     * @param userTokenAccountOut The public key of the user's output token account.
     * @param perpetuals          The public key of the perpetuals account.
     * @param pool                The public key of the pool account.
     * @param custodyIn           The public key of the input custody account.
     * @param custodyOut          The public key of the output custody account.
     * @param amountIn            The amount of input tokens.
     * @param minAmountOut        The minimum amount of output tokens expected.
     * @param params              Additional swap parameters.
     * @return A TransactionInstruction object for swapping tokens.
     */
    public static TransactionInstruction swap2(
            PublicKey user,
            PublicKey userTokenAccountIn,
            PublicKey userTokenAccountOut,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey custodyIn,
            PublicKey custodyOut,
            long amountIn,
            long minAmountOut,
            byte[] params // Serialized swap params
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, false));
        keys.add(new AccountMeta(userTokenAccountIn, false, true));
        keys.add(new AccountMeta(userTokenAccountOut, false, true));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(custodyIn, false, true));
        keys.add(new AccountMeta(custodyOut, false, true));
        keys.add(new AccountMeta(new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), false, false));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.swap2.name());

        int paramsLength = params != null ? params.length : 0;

        ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 8 + 4 + paramsLength); // discriminator + amountIn + minAmountOut + params length + params
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putLong(amountIn);
        buffer.putLong(minAmountOut);
        buffer.putInt(paramsLength); // u32 length
        if (paramsLength > 0) {
            buffer.put(params);
        }

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates an instruction to add liquidity using version 2 of the addLiquidity instruction.
     *
     * @param user               The public key of the user account.
     * @param userTokenAccount   The public key of the user's token account.
     * @param userLpTokenAccount The public key of the user's LP token account.
     * @param perpetuals         The public key of the perpetuals account.
     * @param pool               The public key of the pool account.
     * @param custody            The public key of the custody account.
     * @param lpTokenMint        The public key of the LP token mint account.
     * @param associatedProgram  The public key of the associated token program.
     * @param amount             The amount of tokens to add.
     * @param minLpAmount        The minimum amount of LP tokens expected.
     * @param params             Additional parameters.
     * @return A TransactionInstruction object for adding liquidity.
     */
    public static TransactionInstruction addLiquidity2(
            PublicKey user,
            PublicKey userTokenAccount,
            PublicKey userLpTokenAccount,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey custody,
            PublicKey lpTokenMint,
            PublicKey associatedProgram,
            long amount,
            long minLpAmount,
            byte[] params // Serialized add liquidity params
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, true));
        keys.add(new AccountMeta(userTokenAccount, false, true));
        keys.add(new AccountMeta(userLpTokenAccount, false, true));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(custody, false, true));
        keys.add(new AccountMeta(lpTokenMint, false, true));
        keys.add(new AccountMeta(associatedProgram, false, false));
        keys.add(new AccountMeta(new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), false, false));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.addLiquidity2.name());

        int paramsLength = params != null ? params.length : 0;

        ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 8 + 4 + paramsLength); // discriminator + amount + minLpAmount + params length + params
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putLong(amount);
        buffer.putLong(minLpAmount);
        buffer.putInt(paramsLength);
        if (paramsLength > 0) {
            buffer.put(params);
        }

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Creates an instruction to remove liquidity using version 2 of the removeLiquidity instruction.
     *
     * @param user               The public key of the user account.
     * @param userTokenAccount   The public key of the user's token account.
     * @param userLpTokenAccount The public key of the user's LP token account.
     * @param perpetuals         The public key of the perpetuals account.
     * @param pool               The public key of the pool account.
     * @param custody            The public key of the custody account.
     * @param lpTokenMint        The public key of the LP token mint account.
     * @param associatedProgram  The public key of the associated token program.
     * @param lpAmount           The amount of LP tokens to remove.
     * @param minAmountOut       The minimum amount of tokens expected.
     * @param params             Additional parameters.
     * @return A TransactionInstruction object for removing liquidity.
     */
    public static TransactionInstruction removeLiquidity2(
            PublicKey user,
            PublicKey userTokenAccount,
            PublicKey userLpTokenAccount,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey custody,
            PublicKey lpTokenMint,
            PublicKey associatedProgram,
            long lpAmount,
            long minAmountOut,
            byte[] params // Serialized remove liquidity params
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, true));
        keys.add(new AccountMeta(userTokenAccount, false, true));
        keys.add(new AccountMeta(userLpTokenAccount, false, true));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(custody, false, true));
        keys.add(new AccountMeta(lpTokenMint, false, true));
        keys.add(new AccountMeta(associatedProgram, false, false));
        keys.add(new AccountMeta(new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), false, false));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.removeLiquidity2.name());

        int paramsLength = params != null ? params.length : 0;

        ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 8 + 4 + paramsLength); // discriminator + lpAmount + minAmountOut + params length + params
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putLong(lpAmount);
        buffer.putLong(minAmountOut);
        buffer.putInt(paramsLength);
        if (paramsLength > 0) {
            buffer.put(params);
        }

        return createTransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    // Implement other methods such as createIncreasePositionRequest, updateIncreasePositionRequest, etc.

    /**
     * Creates an instruction to create an increase position request.
     *
     * @param user               The public key of the user account.
     * @param userTokenAccount   The public key of the user's token account.
     * @param perpetuals         The public key of the perpetuals account.
     * @param pool               The public key of the pool account.
     * @param position           The public key of the position account.
     * @param custody            The public key of the custody account.
     * @param collateralCustody  The public key of the collateral custody account.
     * @param collateralAccount  The public key of the collateral account.
     * @param amountIn           The amount of input tokens.
     * @param leverage           The leverage to apply.
     * @param params             Additional parameters.
     * @return A TransactionInstruction object for creating an increase position request.
     */
    public static TransactionInstruction createIncreasePositionRequest(
            PublicKey user,
            PublicKey userTokenAccount,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey position,
            PublicKey custody,
            PublicKey collateralCustody,
            PublicKey collateralAccount,
            long amountIn,
            long leverage,
            byte[] params // Serialized increase position params
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(user, true, true));
        keys.add(new AccountMeta(userTokenAccount, false, true));
        keys.add(new AccountMeta(perpetuals, false, true));
        keys.add(new AccountMeta(pool, false, true));
        keys.add(new AccountMeta(position, false, true));
        keys.add(new AccountMeta(custody, false, true));
        keys.add(new AccountMeta(collateralCustody, false, true));
        keys.add(new AccountMeta(collateralAccount, false, true));
        keys.add(new AccountMeta(new PublicKey("SysvarRent111111111111111111111111111111111"), false, false));
        keys.add(new AccountMeta(new PublicKey("11111111111111111111111111111111"), false, false));

        byte[] instructionDiscriminator = getInstructionDiscriminator(InstructionType.createIncreasePositionRequest.name());

        int paramsLength = params != null ? params.length : 0;

        ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 8 + 4 + paramsLength); // discriminator + amountIn + leverage + params length + params
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(instructionDiscriminator);
        buffer.putLong(amountIn);
        buffer.putLong(leverage);
        buffer.putInt(paramsLength);
        if (paramsLength > 0) {
            buffer.put(params);
        }

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