import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;

@Slf4j
public class OpenBookTest {

    private final RpcClient rpcClient = new RpcClient(Cluster.BLOCKDAEMON);

    @Test
    public void openBookTest() {
        log.info("Hello");
    }
}
