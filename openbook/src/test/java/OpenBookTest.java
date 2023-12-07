import com.mmorrell.openbook.model.OpenBookMarket;
import com.mmorrell.openbook.program.OpenbookProgram;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ProgramAccount;

import java.util.List;

@Slf4j
public class OpenBookTest {

    private final RpcClient client = new RpcClient("https://mainnet.helius-rpc.com/?api-key=a778b653-bdd6-41bc-8cda-0c7377faf1dd");
    private final static byte[] MARKET_DISCRIMINATOR = {
            (byte) 0xDB, (byte) 0xBE, (byte) 0xD5, (byte) 0x37, (byte) 0x00, (byte) 0xE3,
            (byte) 0xC6, (byte) 0x9A
    };

    @Test
    public void openBookV2Test() throws RpcException {
        final List<ProgramAccount> markets = client.getApi().getProgramAccountsBase64(
                OpenbookProgram.OPENBOOK_V2_PROGRAM_ID,
                0,
                Base58.encode(MARKET_DISCRIMINATOR)
        );

        log.info("# of OpenBook v2 markets: " + markets.size());
        markets.forEach(programAccount -> {
            OpenBookMarket openBookMarket = OpenBookMarket.readOpenBookMarket(
                    programAccount.getAccount().getDecodedData(),
                    new PublicKey(programAccount.getPubkey())
            );
            log.info("Market: {}, Detail: {}", openBookMarket.getMarketId(), openBookMarket);
        });
    }

    @Test
    public void openBookGetMarketsTest() {

    }
}
