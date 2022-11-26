import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.util.Base64;

public class PhoenixTest {

    private final RpcClient client = new RpcClient(Cluster.MAINNET);

    @Test
    public void phoenixTest() throws RpcException {
        // Bids for market oTVAoRCiHnfEds5MTPerZk6VunEp24bCae8oSVrQmSU
        PublicKey publicKey = new PublicKey("oTVAoRCiHnfEds5MTPerZk6VunEp24bCae8oSVrQmSU");
        System.out.println(publicKey.toBase58());

        byte[] data = Base64.getDecoder().decode(
                client.getApi().getAccountInfo(publicKey).getValue().getData().get(0)
        );
    }

}
