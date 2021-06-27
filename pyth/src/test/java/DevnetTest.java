import ch.openserum.pyth.manager.PythManager;
import ch.openserum.pyth.model.MappingAccount;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;

import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public class DevnetTest {

    private final RpcClient client = new RpcClient("https://api.devnet.solana.com");
    private final PythManager pythManager = new PythManager(client);
    private static final Logger LOGGER = Logger.getLogger(DevnetTest.class.getName());
    private static final int PYTH_MAGIC_NUMBER = (int) Long.parseLong("a1b2c3d4", 16);
    private static final int EXPECTED_PYTH_VERSION = 1;
    private static final PublicKey TEST_MAPPING_ACCOUNT = PublicKey.valueOf(
            "ArppEFcsybCLE8CRtQJLQ9tLv2peGmQoKWFuiUWm4KBP"
    );

    @Test
    public void pythTest() {
        final MappingAccount mappingAccount = pythManager.getMappingAccount(TEST_MAPPING_ACCOUNT);

        // Magic Number
        int magicInt = mappingAccount.getMagicNumber();
        LOGGER.info(
                String.format(
                        "Magic number %d",
                        magicInt
                )
        );
        assertEquals(PYTH_MAGIC_NUMBER, magicInt);

        // Version
        int version = mappingAccount.getVersion();
        LOGGER.info(
                String.format(
                        "Version %d",
                        version
                )
        );
        assertEquals(EXPECTED_PYTH_VERSION, 1);
    }
}
