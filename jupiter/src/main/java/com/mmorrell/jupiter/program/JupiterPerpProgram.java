package com.mmorrell.jupiter.program;

import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.Program;
import org.p2p.solanaj.programs.SystemProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static com.mmorrell.jupiter.util.JupiterUtil.getInstructionDiscriminator;

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
    public static final PublicKey SYSTEM_PROGRAM_ID = SystemProgram.PROGRAM_ID;
    public static final PublicKey TOKEN_PROGRAM_ID = new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");

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
     * 1. init instruction
     * Accounts:
     * - upgradeAuthority (isSigner: true, isWritable: true)
     * - admin (isSigner: false, isWritable: false)
     * - transferAuthority (isSigner: false, isWritable: true)
     * - perpetuals (isSigner: false, isWritable: true)
     * - perpetualsProgram (isSigner: false, isWritable: false)
     * - perpetualsProgramData (isSigner: false, isWritable: false)
     * - systemProgram (isSigner: false, isWritable: false)
     * - tokenProgram (isSigner: false, isWritable: false)
     * Arguments:
     * - params: InitParams
     */
    public static TransactionInstruction init(
            PublicKey upgradeAuthority,
            PublicKey admin,
            PublicKey transferAuthority,
            PublicKey perpetuals,
            PublicKey perpetualsProgram,
            PublicKey perpetualsProgramData,
            InitParams params
    ) {
        List<AccountMeta> accounts = new ArrayList<>();
        accounts.add(new AccountMeta(upgradeAuthority, true, true));
        accounts.add(new AccountMeta(admin, false, false));
        accounts.add(new AccountMeta(transferAuthority, false, true));
        accounts.add(new AccountMeta(perpetuals, false, true));
        accounts.add(new AccountMeta(perpetualsProgram, false, false));
        accounts.add(new AccountMeta(perpetualsProgramData, false, false));
        accounts.add(new AccountMeta(SYSTEM_PROGRAM_ID, false, false));
        accounts.add(new AccountMeta(TOKEN_PROGRAM_ID, false, false));

        byte[] discriminator = getInstructionDiscriminator("init");
        
        // Serialize params using Borsh
        byte[] paramsData = params.serialize();

        // Combine discriminator and paramsData
        ByteBuffer buffer = ByteBuffer.allocate(discriminator.length + paramsData.length);
        buffer.put(discriminator);
        buffer.put(paramsData);

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                accounts,
                buffer.array()
        );
    }

    public static TransactionInstruction addPool(
            PublicKey admin,
            PublicKey transferAuthority,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey lpTokenMint,
            PublicKey systemProgram,
            PublicKey tokenProgram,
            PublicKey rent,
            AddPoolParams params
    ) {
        List<AccountMeta> accounts = new ArrayList<>();
        accounts.add(new AccountMeta(admin, true, true));
        accounts.add(new AccountMeta(transferAuthority, false, false));
        accounts.add(new AccountMeta(perpetuals, false, true));
        accounts.add(new AccountMeta(pool, false, true));
        accounts.add(new AccountMeta(lpTokenMint, false, true));
        accounts.add(new AccountMeta(systemProgram, false, false));
        accounts.add(new AccountMeta(tokenProgram, false, false));
        accounts.add(new AccountMeta(rent, false, false));

        byte[] discriminator = getInstructionDiscriminator("add_pool");
        
        // Serialize params
        byte[] paramsData = params.serialize();

        ByteBuffer buffer = ByteBuffer.allocate(discriminator.length + paramsData.length);
        buffer.put(discriminator);
        buffer.put(paramsData);

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                accounts,
                buffer.array()
        );
    }

    public static TransactionInstruction addCustody(
            PublicKey admin,
            PublicKey transferAuthority,
            PublicKey perpetuals,
            PublicKey pool,
            PublicKey custody,
            PublicKey custodyTokenAccount,
            PublicKey custodyTokenMint,
            PublicKey systemProgram,
            PublicKey tokenProgram,
            PublicKey rent,
            AddCustodyParams params // Custom class representing AddCustodyParams
    ) {
        List<AccountMeta> accounts = new ArrayList<>();
        accounts.add(new AccountMeta(admin, true, true));
        accounts.add(new AccountMeta(transferAuthority, false, false));
        accounts.add(new AccountMeta(perpetuals, false, false));
        accounts.add(new AccountMeta(pool, false, true));
        accounts.add(new AccountMeta(custody, false, true));
        accounts.add(new AccountMeta(custodyTokenAccount, false, true));
        accounts.add(new AccountMeta(custodyTokenMint, false, false));
        accounts.add(new AccountMeta(systemProgram, false, false));
        accounts.add(new AccountMeta(tokenProgram, false, false));
        accounts.add(new AccountMeta(rent, false, false));

        byte[] discriminator = getInstructionDiscriminator("add_custody");

        byte[] paramsData = params.serialize();

        ByteBuffer buffer = ByteBuffer.allocate(discriminator.length + paramsData.length);
        buffer.put(discriminator);
        buffer.put(paramsData);

        return new TransactionInstruction(
                JUPITER_PERP_PROGRAM_ID,
                accounts,
                buffer.array()
        );
    }
    
    public static class InitParams {
        public boolean allowSwap;
        public boolean allowAddLiquidity;
        public boolean allowRemoveLiquidity;
        public boolean allowIncreasePosition;
        public boolean allowDecreasePosition;
        public boolean allowCollateralWithdrawal;
        public boolean allowLiquidatePosition;

        public byte[] serialize() {
            ByteBuffer buffer = ByteBuffer.allocate(7);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(allowSwap ? (byte) 1 : (byte) 0);
            buffer.put(allowAddLiquidity ? (byte) 1 : (byte) 0);
            buffer.put(allowRemoveLiquidity ? (byte) 1 : (byte) 0);
            buffer.put(allowIncreasePosition ? (byte) 1 : (byte) 0);
            buffer.put(allowDecreasePosition ? (byte) 1 : (byte) 0);
            buffer.put(allowCollateralWithdrawal ? (byte) 1 : (byte) 0);
            buffer.put(allowLiquidatePosition ? (byte) 1 : (byte) 0);
            return buffer.array();
        }
    }

    public static class AddPoolParams {
        public String name;

        public byte[] serialize() {
            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.allocate(4 + nameBytes.length);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(nameBytes.length);
            buffer.put(nameBytes);
            return buffer.array();
        }

        public static class Limit {

        }
    }

    public static class AddCustodyParams {
        public byte oracleType;
        public byte[] oracleParams;

        public byte[] serialize() {
            ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + oracleParams.length);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(oracleType);
            buffer.putInt(oracleParams.length);
            buffer.put(oracleParams);
            return buffer.array();
        }
    }
}