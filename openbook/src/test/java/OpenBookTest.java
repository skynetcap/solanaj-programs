import com.mmorrell.openbook.program.OpenbookProgram;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

@Slf4j
public class OpenBookTest {

    private final RpcClient rpcClient = new RpcClient(Cluster.BLOCKDAEMON);
    private final Account testAccount = new Account();

    @Test
    @Ignore
    public void openBookTest() {
        log.info(testAccount.getPublicKey().toBase58());

        Transaction tx = new Transaction();
        tx.addInstruction(OpenbookProgram.createOpenOrdersIndexer(testAccount));

        try {
            String txId = rpcClient.getApi().sendTransaction(tx, testAccount);
            log.info(txId);
        } catch (RpcException e) {
            throw new RuntimeException(e);
        }

    }
}
