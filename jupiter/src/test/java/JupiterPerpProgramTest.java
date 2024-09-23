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
    private PublicKey newContract;

    @BeforeEach
    void setUp() {
        program = new JupiterPerpProgram();
        initializer = new PublicKey("3N2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        user = new PublicKey("4N2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        position = new PublicKey("5N2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        collateral = new PublicKey("6N2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        collateralMint = new PublicKey("7N2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        liquidator = new PublicKey("8N2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        pool = new PublicKey("9N2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        liquidity = new PublicKey("AN2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        source = new PublicKey("BN2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        destination = new PublicKey("CN2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
        newContract = new PublicKey("DN2g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g1Z1g"); // Valid base58
    }
}