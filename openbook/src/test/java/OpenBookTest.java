import com.mmorrell.openbook.model.OpenBookMarket;
import com.mmorrell.openbook.util.Keccak;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.junit.Test;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ProgramAccount;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class OpenBookTest {

    private final RpcClient client = new RpcClient("https://mainnet.helius-rpc.com/?api-key=a778b653-bdd6-41bc-8cda-0c7377faf1dd");

    @Test
    public void openBookV2Test() throws RpcException {
        byte[] discriminator = {
                (byte)0xDB, (byte)0xBE, (byte)0xD5, (byte)0x37, (byte)0x00, (byte)0xE3,
                (byte)0xC6, (byte)0x9A
        };

        final List<ProgramAccount> markets = client.getApi().getProgramAccountsBase64(
                new PublicKey("opnb2LAfJYbRMAHHvqjCwQxanZn7ReEHp1k81EohpZb"),
                0,
                Base58.encode(discriminator)
        );

        log.info("Number of markets: " + markets.size());
        markets.forEach(programAccount -> {
            log.info("Market: " + programAccount.getPubkey());
            OpenBookMarket openBookMarket = OpenBookMarket.readOpenBookMarket(
                    programAccount.getAccount().getDecodedData(),
                    new PublicKey(programAccount.getPubkey())
            );

            log.info("Deserialized: {}", openBookMarket);

        });
    }

    @Test
    public void openBookGetMarketsTest() {

    }

    public static String getDiscriminator(String input) {
        Keccak keccak = new Keccak(256);
        keccak.update(new PublicKey("opnb2LAfJYbRMAHHvqjCwQxanZn7ReEHp1k81EohpZb").toByteArray());
        keccak.update(input.getBytes());

        ByteBuffer keccakBuffer = keccak.digest();
        keccakBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] keccakBytes = keccakBuffer.array();

        return Base58.encode(Arrays.copyOfRange(keccakBytes, 0, 8));
    }
}
