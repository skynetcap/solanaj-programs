import ch.openserum.pyth.utils.PythUtils;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;

import java.util.Base64;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public class DevnetTest {

    private final RpcClient client = new RpcClient("https://api.devnet.solana.com");
    private static final int PYTH_MAGIC_NUMBER = (int) Long.parseLong("a1b2c3d4", 16);
    private static final int EXPECTED_PYTH_VERSION = 1;
    private static final int MAGIC_NUMBER_OFFSET = 0;
    private static final int VERSION_OFFSET = MAGIC_NUMBER_OFFSET + PythUtils.INT32_SIZE;

    @Test
    public void pythTest() throws RpcException {
        AccountInfo accountInfo = client.getApi().getAccountInfo(
                PublicKey.valueOf("ArppEFcsybCLE8CRtQJLQ9tLv2peGmQoKWFuiUWm4KBP")
        );

        byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));

        // Magic Number
        int magicInt = PythUtils.readInt32(data, MAGIC_NUMBER_OFFSET);
        Logger.getAnonymousLogger().info(
                String.format(
                        "Magic number %d",
                        magicInt
                )
        );
        assertEquals(PYTH_MAGIC_NUMBER, magicInt);

        // Version
        int version = PythUtils.readInt32(data, VERSION_OFFSET);
        Logger.getAnonymousLogger().info(
                String.format(
                        "Version %d",
                        version
                )
        );
        assertEquals(EXPECTED_PYTH_VERSION, 1);
    }
}
