import com.mmorrell.openbook.program.OpenbookProgram;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

@Slf4j
public class OpenBookTest {

    private final RpcClient rpcClient = new RpcClient(Cluster.BLOCKDAEMON);
    private final Account testAccount = Account.fromJson("[255,181,248,82,179,247,83,90,145,105,170,35,64,239,41,71,160,49,254,240,122,128,229,155,52,230,53,193,23,246,83,29,11,116,195,237,133,152,251,78,144,10,130,201,91,66,43,143,101,167,69,56,134,4,6,47,31,100,212,108,71,177,73,156]");

    @Test
    public void openBookTest() throws Exception {
        log.info(testAccount.getPublicKey().toBase58());

        Transaction tx = new Transaction();
        tx.addInstruction(OpenbookProgram.createOpenOrdersIndexer(testAccount, SystemProgram.PROGRAM_ID));

        try {
            String txId = rpcClient.getApi().sendTransaction(tx, testAccount);
            log.info(txId);
        } catch (RpcException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void openBookCreateMarketTest() throws Exception {
        log.info(testAccount.getPublicKey().toBase58());

        // create base vault and create quote vault
        // they have to be token accounts before using them


        Transaction tx = new Transaction();
        tx.addInstruction(
                OpenbookProgram.createMarket(
                        testAccount,
                        new PublicKey("So11111111111111111111111111111111111111112"),
                        new PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"),
                        null,
                        null
                )
        );

        try {
            String txId = rpcClient.getApi().sendTransaction(tx, testAccount);
            log.info(txId);
        } catch (RpcException e) {
            throw new RuntimeException(e);
        }

    }
}
