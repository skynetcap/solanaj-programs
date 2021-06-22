import org.bitcoinj.core.Utils;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public class DevnetTest {

    private final RpcClient client = new RpcClient("https://api.devnet.solana.com");
    private static final int PYTH_MAGIC_NUMBER = (int) Long.parseLong("a1b2c3d4", 16);

    @Test
    public void pythTest() throws RpcException {
        AccountInfo accountInfo = client.getApi().getAccountInfo(
                PublicKey.valueOf("ArppEFcsybCLE8CRtQJLQ9tLv2peGmQoKWFuiUWm4KBP")
        );

        byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
        byte[] magicNumber = ByteUtils.readBytes(data, 0, 4);

        ByteBuffer wrapped = ByteBuffer.wrap(magicNumber);
        wrapped.order(ByteOrder.LITTLE_ENDIAN);
        int magicInt = wrapped.getInt();

        Logger.getAnonymousLogger().info(
                String.format(
                        "Magic number %d, Hex = %s",
                        magicInt,
                        ByteUtils.bytesToHex(Utils.reverseBytes(wrapped.array()))
                )
        );

        assertEquals(PYTH_MAGIC_NUMBER, magicInt);
    }

}
