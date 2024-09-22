import com.google.common.io.Files;
import com.mmorrell.jupiter.model.*;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.Memcmp;
import org.p2p.solanaj.rpc.types.ProgramAccount;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Jupiter Perpetuals positions.
 */
@Slf4j
public class JupiterTest {

    private final RpcClient client = new RpcClient("https://mainnet.helius-rpc.com/?api-key=a778b653-bdd6-41bc-8cda-0c7377faf1dd");

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

    @Test
    @Disabled
    public void testGetAllJupiterPerpPositions() throws RpcException {
        PublicKey programId = new PublicKey("PERPHjGBqRHArX4DySjwM6UJHiR3sWAatqfdBS2qQJu");

        // Get the discriminator for the Position account
        byte[] positionDiscriminator = getAccountDiscriminator("Position");

        // Create a memcmp filter for the discriminator at offset 0
        Memcmp memcmpFilter = new Memcmp(0, Base58.encode(positionDiscriminator));

        // Get all program accounts matching the filters
        List<ProgramAccount> positionAccounts = client.getApi().getProgramAccounts(
                programId,
                Collections.singletonList(memcmpFilter),
                216
        );

        List<JupiterPerpPosition> positions = new ArrayList<>();
        for (ProgramAccount account : positionAccounts) {
            // Decode the account data
            byte[] data = account.getAccount().getDecodedData();

            // Deserialize the data into JupiterPerpPosition
            JupiterPerpPosition position = JupiterPerpPosition.fromByteArray(data);

            if (position.getSizeUsd() > 0) {
                // Add to the list
                positions.add(position);
            }
        }

        positions.sort(Comparator.comparingLong(JupiterPerpPosition::getSizeUsd));

        // Log the positions
        for (JupiterPerpPosition position : positions) {
            double leverage = (double) position.getSizeUsd() / position.getCollateralUsd();
            log.info("Owner: {}, Size USD: {}, Leverage: {}", position.getOwner().toBase58(), position.getSizeUsd(), leverage);
        }
    }

    /**
     * Calculates the account discriminator for a given account name.
     *
     * @param accountName the name of the account.
     * @return the first 8 bytes of the SHA-256 hash of "account:<accountName>".
     */
    private byte[] getAccountDiscriminator(String accountName) {
        String preimage = "account:" + accountName;
        try {
            MessageDigest hasher = MessageDigest.getInstance("SHA-256");
            hasher.update(preimage.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOfRange(hasher.digest(), 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found for discriminator calculation.");
        }
    }

    @Test
    public void testJupiterPoolDeserialization() throws RpcException {
        PublicKey poolPublicKey = new PublicKey("5BUwFW4nRbftYTDMbgxykoFWqWHPzahFSNAaaaJtVKsq");

        // Fetch the account data
        AccountInfo accountInfo = client.getApi().getAccountInfo(poolPublicKey);

        assertNotNull(accountInfo, "Account info should not be null");

        byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));

        // Deserialize the data into JupiterPool
        JupiterPool pool = JupiterPool.fromByteArray(data);

        log.info("Deserialized JupiterPool: {}", pool);

        // Assertions
        assertNotNull(pool);
        assertEquals("Pool", pool.getName());
        assertEquals(5, pool.getCustodies().size());
        assertTrue(pool.getAumUsd() > 0);
        assertNotNull(pool.getLimit());
        assertNotNull(pool.getFees());
        assertNotNull(pool.getPoolApr());
        assertEquals(45, pool.getMaxRequestExecutionSec());
        assertEquals((byte) 252, pool.getBump());
        assertEquals((byte) 254, pool.getLpTokenBump());
        assertEquals(1689677832, pool.getInceptionTime());
    }

    @Test
    public void testJupiterCustodyDeserialization() throws RpcException {
        PublicKey custodyPublicKey = new PublicKey("7xS2gz2bTp3fwCC7knJvUWTEU9Tycczu6VhJYKgi1wdz");

        // Fetch the account data
        AccountInfo accountInfo = client.getApi().getAccountInfo(custodyPublicKey);

        assertNotNull(accountInfo, "Account info should not be null");

        byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));

        // Deserialize the data into JupiterCustody
        JupiterCustody custody = JupiterCustody.fromByteArray(data);

        // Assertions
        assertNotNull(custody);
        assertNotNull(custody.getPool());
        assertNotNull(custody.getMint());
        assertNotNull(custody.getTokenAccount());

        log.info("Deserialized JupiterCustody: {}", custody);
    }

    @Test
    public void testJupiterPerpetualsDeserialization() throws RpcException, IOException {
        PublicKey perpetualsPublicKey = new PublicKey("H4ND9aYttUVLFmNypZqLjZ52FYiGvdEB45GmwNoKEjTj");

        // Fetch the account data
        AccountInfo accountInfo = client.getApi().getAccountInfo(perpetualsPublicKey);

        assertNotNull(accountInfo, "Account info should not be null");

        byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));

        // Deserialize the data into JupiterPerpetuals
        JupiterPerpetuals perpetuals = JupiterPerpetuals.fromByteArray(data);

        // Assertions
        assertNotNull(perpetuals);
        assertNotNull(perpetuals.getPermissions());
        assertNotNull(perpetuals.getPool());
        assertNotNull(perpetuals.getAdmin());

        for (int i = 0; i < data.length; i++) {
            try {
                PublicKey pk = new PublicKey(Arrays.copyOfRange(data, i, i + 32));
                if (pk.toBase58().equalsIgnoreCase("5BUwFW4nRbftYTDMbgxykoFWqWHPzahFSNAaaaJtVKsq")) {
                    log.info("FOUND OFFSET 1 (POOL): {}", i);
                }
                if (pk.toBase58().equalsIgnoreCase("9hdBK7FUzv4NjZbtYfm39F5utJyFsmCwbF9Mow5Pr1sN")) {
                    log.info("FOUND OFFSET 2 (ADMIN): {}", i);
                }
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }

        log.info("Deserialized JupiterPerpetuals: {}", perpetuals);

        // Assuming one pool for now. The vector deserialization seemed off.
        assertEquals("5BUwFW4nRbftYTDMbgxykoFWqWHPzahFSNAaaaJtVKsq", perpetuals.getPool().toBase58());

    }

    @Test
    public void testJupiterPositionRequestDeserialization() throws RpcException, IOException {
        PublicKey positionRequestPublicKey = new PublicKey("APYrGNtsTTMpNNBQBpALxYnwYfKDQyFocf3d1j6jkuzf");

        // Fetch the account data
        AccountInfo accountInfo = client.getApi().getAccountInfo(positionRequestPublicKey);

        assertNotNull(accountInfo, "Account info should not be null");

        byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
        Files.write(data, new File("jupiterPositionRequest.bin"));

        // Deserialize the data into JupiterPositionRequest
        JupiterPositionRequest positionRequest = JupiterPositionRequest.fromByteArray(data);

        // Assertions
        assertNotNull(positionRequest);
        assertNotNull(positionRequest.getOwner());
        assertNotNull(positionRequest.getPool());
        assertNotNull(positionRequest.getCustody());
        assertTrue(positionRequest.getOpenTime() > 0);
        assertTrue(positionRequest.getUpdateTime() > 0);

        log.info("Deserialized JupiterPositionRequest: {}", positionRequest);
    }

    @Test
    @Disabled
    public void testJupiterTestOracleDeserialization() throws RpcException {
        PublicKey testOraclePublicKey = new PublicKey("YourTestOraclePublicKeyHere");

        // Fetch the account data
        AccountInfo accountInfo = client.getApi().getAccountInfo(testOraclePublicKey);

        assertNotNull(accountInfo, "Account info should not be null");

        byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));

        // Deserialize the data into JupiterTestOracle
        JupiterTestOracle testOracle = JupiterTestOracle.fromByteArray(data);

        // Assertions
        assertNotNull(testOracle);
        assertTrue(testOracle.getPrice() != 0);
        assertTrue(testOracle.getPublishTime() > 0);
        // Add more assertions as needed
    }
    
}
