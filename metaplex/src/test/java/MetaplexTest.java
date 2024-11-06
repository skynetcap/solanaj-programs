import com.mmorrell.metaplex.manager.MetaplexManager;
import com.mmorrell.metaplex.model.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class MetaplexTest {
    private static final PublicKey ORE_MINT = new PublicKey("oreoU2P8bN6jkk3jbaiVxYnG1dCXcYxwhwyK9jSybcp");
    private final RpcClient rpcClient = new RpcClient(Cluster.MAINNET);
    private final MetaplexManager metaplexManager = new MetaplexManager(rpcClient);

    @BeforeAll
    public static void beforeClass() throws InterruptedException {
        // Prevent RPCPool rate limit
        Thread.sleep(2000L);
    }

    @Test
    public void metadataTest() {
        final Optional<Metadata> optionalMetadata = metaplexManager.getTokenMetadata(ORE_MINT);
        assertTrue(optionalMetadata.isPresent());

        final Metadata metadata = optionalMetadata.get();
        log.info(
                String.format(
                        "Metadata: %s",
                        metadata
                )
        );
    }

    @Test
    public void metadataEmojiSymbolTest() {
        final Optional<Metadata> optionalMetadata = metaplexManager.getTokenMetadata(
                new PublicKey("Fc9Lf5TeCQr3xUHL2zpgVSjHkq2GD25bUxVa6oDSpump")
        );
        assertTrue(optionalMetadata.isPresent());

        final Metadata metadata = optionalMetadata.get();
        log.info(
                String.format(
                        "Metadata: %s",
                        metadata
                )
        );
    }

    @Test
    public void metadataMultipleTest() {
        var metadata = metaplexManager.getTokenMetadata(
                List.of(
                        ORE_MINT,
                        new PublicKey("5mbK36SZ7J19An8jFochhQS4of8g6BwUjbeCSxBSoWdp")
                )
        );

        assertEquals(2, metadata.size());

        log.info("Multiple metadatas: {}", metadata);
    }
}
