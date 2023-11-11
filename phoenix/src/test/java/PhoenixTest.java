import com.mmorrell.phoenix.model.PhoenixMarketHeader;
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

    private final RpcClient client = new RpcClient("https://holy-boldest-cloud.solana-mainnet.quiknode.pro/7e4d60d03a33dfd0562901465716cd612b940903/");

    @Test
    public void phoenixGetMarketsTest() throws RpcException {
        // GPA for all markets
        final List<ProgramAccount> markets = client.getApi().getProgramAccountsBase64(
                PhoenixProgram.PHOENIX_PROGRAM_ID,
                0,
                getDiscriminator("phoenix::program::accounts::MarketHeader")
        );

        System.out.println("Number of markets: " + markets.size());
        markets.forEach(programAccount -> {
            System.out.println("Market: " + programAccount.getPubkey());

            final PhoenixMarketHeader phoenixMarketHeader = PhoenixMarketHeader.readPhoenixMarketHeader(
                    Arrays.copyOfRange(
                            programAccount.getAccount().getDecodedData(),
                            0,
                            PhoenixMarketHeader.MARKET_HEADER_SIZE
                    )
            );
            System.out.println(phoenixMarketHeader);

        });
    }

    private String getDiscriminator(String input) {
        Keccak keccak = new Keccak(256);
        keccak.update(PhoenixProgram.PHOENIX_PROGRAM_ID.toByteArray());
        keccak.update(input.getBytes());

        ByteBuffer keccakBuffer = keccak.digest();
        keccakBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] keccakBytes = keccakBuffer.array();

        return Base58.encode(Arrays.copyOfRange(keccakBytes, 0, 8));
    }
}