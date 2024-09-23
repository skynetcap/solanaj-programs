import com.mmorrell.jupiter.program.JupiterPerpProgram;
import com.mmorrell.jupiter.program.JupiterPerpProgram.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for JupiterPerpProgram.
 */
class JupiterPerpProgramTest {

    private JupiterPerpProgram program;
    private PublicKey initializer;
    private PublicKey user;
    private PublicKey position;
    private PublicKey collateral;
    private PublicKey collateralMint;
    private PublicKey liquidator;
    private PublicKey pool;
    private PublicKey liquidity;
    private PublicKey source;
    private PublicKey destination;

    @BeforeEach
    void setUp() {
        program = new JupiterPerpProgram();
        initializer = new PublicKey("3N2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        user = new PublicKey("4N2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        position = new PublicKey("5N2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        collateral = new PublicKey("6N2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        collateralMint = new PublicKey("7N2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        liquidator = new PublicKey("8N2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        pool = new PublicKey("9N2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        liquidity = new PublicKey("AN2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        source = new PublicKey("BN2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        destination = new PublicKey("CN2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
    }

    @Test
    void testInitializeInstruction() {
        InitializeConfig config = new InitializeConfig(5, 100);
        TransactionInstruction instruction = program.initialize(initializer, config);

        assertNotNull(instruction);
        assertEquals(JupiterPerpProgram.JUPITER_PERP_PROGRAM_ID, instruction.getProgramId());
        assertEquals(2, instruction.getKeys().size());

        byte[] data = instruction.getData();
        assertEquals(1 + InitializeConfig.BYTES, data.length);
        assertEquals(0, data[0]); // INITIALIZE instruction type
    }

    @Test
    void testCreatePositionInstruction() {
        CreatePositionConfig config = new CreatePositionConfig(2.5, 1000, 400);
        TransactionInstruction instruction = program.createPosition(user, position, collateral, collateralMint, config);

        assertNotNull(instruction);
        assertEquals(JupiterPerpProgram.JUPITER_PERP_PROGRAM_ID, instruction.getProgramId());
        assertEquals(5, instruction.getKeys().size());

        byte[] data = instruction.getData();
        assertEquals(1 + CreatePositionConfig.BYTES, data.length);
        assertEquals(1, data[0]); // CREATE_POSITION instruction type
    }

    @Test
    void testClosePositionInstruction() {
        TransactionInstruction instruction = program.closePosition(user, position);

        assertNotNull(instruction);
        assertEquals(JupiterPerpProgram.JUPITER_PERP_PROGRAM_ID, instruction.getProgramId());
        assertEquals(3, instruction.getKeys().size());

        byte[] data = instruction.getData();
        assertEquals(1, data.length);
        assertEquals(2, data[0]); // CLOSE_POSITION instruction type
    }

    @Test
    void testIncreasePositionInstruction() {
        IncreasePositionConfig config = new IncreasePositionConfig(500, 200);
        TransactionInstruction instruction = program.increasePosition(user, position, config);

        assertNotNull(instruction);
        assertEquals(JupiterPerpProgram.JUPITER_PERP_PROGRAM_ID, instruction.getProgramId());
        assertEquals(3, instruction.getKeys().size());

        byte[] data = instruction.getData();
        assertEquals(1 + IncreasePositionConfig.BYTES, data.length);
        assertEquals(3, data[0]); // INCREASE_POSITION instruction type
    }

    @Test
    void testDecreasePositionInstruction() {
        DecreasePositionConfig config = new DecreasePositionConfig(300, 150);
        TransactionInstruction instruction = program.decreasePosition(user, position, config);

        assertNotNull(instruction);
        assertEquals(JupiterPerpProgram.JUPITER_PERP_PROGRAM_ID, instruction.getProgramId());
        assertEquals(3, instruction.getKeys().size());

        byte[] data = instruction.getData();
        assertEquals(1 + DecreasePositionConfig.BYTES, data.length);
        assertEquals(4, data[0]); // DECREASE_POSITION instruction type
    }

    @Test
    void testLiquidatePositionInstruction() {
        // Create a config for liquidating position
        LiquidatePositionConfig config = new LiquidatePositionConfig(100); // Example value

        TransactionInstruction instruction = program.liquidatePosition(liquidator, position, config);

        assertNotNull(instruction);
        assertEquals(JupiterPerpProgram.JUPITER_PERP_PROGRAM_ID, instruction.getProgramId());
        assertEquals(3, instruction.getKeys().size());

        byte[] data = instruction.getData();
        assertEquals(9, data.length);
        assertEquals(5, data[0]); // LIQUIDATE_POSITION instruction type
    }

    @Test
    void testAddLiquidityInstruction() {
        AddLiquidityConfig config = new AddLiquidityConfig(1000, 50);
        TransactionInstruction instruction = program.addLiquidity(user, pool, liquidity, config);

        assertNotNull(instruction);
        assertEquals(JupiterPerpProgram.JUPITER_PERP_PROGRAM_ID, instruction.getProgramId());
        assertEquals(4, instruction.getKeys().size());

        byte[] data = instruction.getData();
        assertEquals(1 + AddLiquidityConfig.BYTES, data.length);
        assertEquals(6, data[0]); // ADD_LIQUIDITY instruction type
    }

    @Test
    void testRemoveLiquidityInstruction() {
        RemoveLiquidityConfig config = new RemoveLiquidityConfig(50, 900);
        TransactionInstruction instruction = program.removeLiquidity(user, pool, liquidity, config);

        assertNotNull(instruction);
        assertEquals(JupiterPerpProgram.JUPITER_PERP_PROGRAM_ID, instruction.getProgramId());
        assertEquals(4, instruction.getKeys().size());

        byte[] data = instruction.getData();
        assertEquals(1 + RemoveLiquidityConfig.BYTES, data.length);
        assertEquals(7, data[0]); // REMOVE_LIQUIDITY instruction type
    }

    @Test
    void testWithdrawCollateralInstruction() {
        WithdrawCollateralConfig config = new WithdrawCollateralConfig(200, 180);
        TransactionInstruction instruction = program.withdrawCollateral(user, position, collateral, config);

        assertNotNull(instruction);
        assertEquals(JupiterPerpProgram.JUPITER_PERP_PROGRAM_ID, instruction.getProgramId());
        assertEquals(4, instruction.getKeys().size());

        byte[] data = instruction.getData();
        assertEquals(1 + WithdrawCollateralConfig.BYTES, data.length);
        assertEquals(8, data[0]); // WITHDRAW_COLLATERAL instruction type
    }

    @Test
    void testSwapInstruction() {
        SwapConfig config = new SwapConfig(500, 450);
        TransactionInstruction instruction = program.swap(user, pool, source, destination, config);

        assertNotNull(instruction);
        assertEquals(JupiterPerpProgram.JUPITER_PERP_PROGRAM_ID, instruction.getProgramId());
        assertEquals(5, instruction.getKeys().size());

        byte[] data = instruction.getData();
        assertEquals(1 + SwapConfig.BYTES, data.length);
        assertEquals(9, data[0]); // SWAP instruction type
    }

    @Test
    void testUpdateFeeRateInstruction() {
        long newFeeRate = 150;
        // Create a config for updating fee rate
        UpdateFeeRateConfig config = new UpdateFeeRateConfig(newFeeRate);

        TransactionInstruction instruction = program.updateFeeRate(initializer, config);

        assertNotNull(instruction);
        assertEquals(JupiterPerpProgram.JUPITER_PERP_PROGRAM_ID, instruction.getProgramId());
        assertEquals(2, instruction.getKeys().size());

        byte[] data = instruction.getData();
        assertEquals(1 + 8, data.length);
        assertEquals(10, data[0]); // UPDATE_FEE_RATE instruction type
    }
}