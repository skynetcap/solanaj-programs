import com.mmorrell.metaplex.manager.MetaplexManager;
import com.mmorrell.metaplex.model.Metadata;
import org.junit.BeforeClass;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;

import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

public class MetaplexTest {

    private static final Logger LOGGER = Logger.getLogger(MetaplexTest.class.getName());
    private static final PublicKey CRIPCO_TOKEN_MINT = new PublicKey("3uejHm24sWmniGA5m4j4S1DVuGqzYBR5DJpevND4mivq");
    private final RpcClient rpcClient = new RpcClient(Cluster.MAINNET);
    private final MetaplexManager metaplexManager = new MetaplexManager(rpcClient);

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        // Prevent RPCPool rate limit
        Thread.sleep(2000L);
    }

    @Test
    public void metadataTest() {
        final Optional<Metadata> optionalMetadata = metaplexManager.getTokenMetadata(CRIPCO_TOKEN_MINT);
        assertTrue(optionalMetadata.isPresent());

        final Metadata metadata = optionalMetadata.get();
        LOGGER.info(
                String.format(
                        "Metadata: %s",
                        metadata
                )
        );
    }
}
