import com.mmorrell.jupiter.model.JupiterPerpPosition;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JupiterTest {

    private final RpcClient client = new RpcClient(Cluster.MAINNET);

    @Test
    public void testProofOfConcept() {
        log.info("Testing proof of concept");
    }

    @Test
    public void testJupiterPerpPositionDeserialization() throws RpcException {
        PublicKey positionPublicKey = new PublicKey("FdqbJAvADUJzZsBFK1ArhV79vXLmpKUMB4oXSrW8rSE");
        PublicKey positionPublicKeyOwner = new PublicKey("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq");

        // Fetch the account data
        AccountInfo accountInfo = client.getApi().getAccountInfo(positionPublicKey);
        
        assertNotNull(accountInfo, "Account info should not be null");
        
        byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
        
        // Deserialize the data into JupiterPerpPosition
        JupiterPerpPosition position = JupiterPerpPosition.fromByteArray(data);
        
        // Log the deserialized position
        log.info("Deserialized JupiterPerpPosition: {}", position);
        
        // Assertions
        assertNotNull(position);
        assertEquals(positionPublicKeyOwner, position.getOwner());
        
        // Add more specific assertions based on expected values
        assertNotNull(position.getPool());
        assertNotNull(position.getCustody());
        assertNotNull(position.getCollateralCustody());
        assertTrue(position.getOpenTime() > 0);
        assertTrue(position.getUpdateTime() > 0);
        assertNotNull(position.getSide());
        assertTrue(position.getPrice() > 0);
        assertTrue(position.getSizeUsd() > 0);
        assertTrue(position.getCollateralUsd() > 0);
        // Add more assertions as needed
    }
}
