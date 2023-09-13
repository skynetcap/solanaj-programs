import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;

@Slf4j
public class OpenBookTest {

    private final RpcClient rpcClient = new RpcClient(Cluster.BLOCKDAEMON);
    private final Account testAccount = Account.fromJson("[255,181,248,82,179,247,83,90,145,105,170,35,64,239,41,71,160,49,254,240,122,128,229,155,52,230,53,193,23,246,83,29,11,116,195,237,133,152,251,78,144,10,130,201,91,66,43,143,101,167,69,56,134,4,6,47,31,100,212,108,71,177,73,156]");

    @Test
    public void openBookTest() {
        log.info("Hello");
        log.info(testAccount.getPublicKey().toBase58());
    }
}
