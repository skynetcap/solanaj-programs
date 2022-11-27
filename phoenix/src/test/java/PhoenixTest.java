import com.mmorrell.phoenix.program.PhoenixProgram;
import com.mmorrell.phoenix.util.Keccak;
import org.bitcoinj.core.Base58;
import org.junit.Test;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ProgramAccount;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

public class PhoenixTest {

    private final RpcClient client = new RpcClient("https://cosmological-black-film.solana-devnet.quiknode.pro/d421efae93a5af6830da22fc2e6ed6a245c5aca1/");

    @Test
    public void phoenixGetMarketsTest() throws RpcException {
        // GPA for all markets
        String baseStr = "phoenix::program::accounts::MarketHeader";

        Keccak keccak = new Keccak(256);
        keccak.update(PhoenixProgram.PHOENIX_PROGRAM_ID.toByteArray());
        keccak.update(baseStr.getBytes());

        // convert to LE
        ByteBuffer keccakBuffer = keccak.digest();
        keccakBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] keccakBytes = keccakBuffer.array();

        // create payload from first 8 bytes
        byte[] input = Arrays.copyOfRange(keccakBytes, 0, 8);
        String payload = Base58.encode(input);
        System.out.println("B58 = " + payload);

        List<ProgramAccount> markets = client.getApi().getProgramAccountsBase64(
                PhoenixProgram.PHOENIX_PROGRAM_ID,
                0,
                payload
        );

        System.out.println("Number of markets: " + markets.size());

        markets.forEach(programAccount -> {
            System.out.println("Market: " + programAccount.getPubkey());
        });
    }

}
