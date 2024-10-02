import com.google.common.io.Files;
import com.mmorrell.jupiter.manager.JupiterManager;
import com.mmorrell.jupiter.model.*;
import com.mmorrell.jupiter.util.JupiterUtil;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Jupiter Perpetuals positions and DCA accounts.
 */
@Slf4j
public class JupiterTest {

    private final RpcClient client = new RpcClient("https://mainnet.helius-rpc.com/?api-key=a778b653-bdd6-41bc-8cda-0c7377faf1dd");

    @BeforeEach
    public void setup() {
        try {
            Thread.sleep(1500L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Disabled
    public void testJupiterPerpPositionDeserialization() throws RpcException {
        PublicKey positionPublicKey = new PublicKey("63sifZpCp9peUq4sfQfxruvKFUCkwLcRfUVVC2mSGDug");
        PublicKey positionPublicKeyOwner = new PublicKey("CMo1gA6YQebnSxXNYK8KawpczFaYLuUgyAf5FRAoryRQ");

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
        byte[] positionDiscriminator = JupiterUtil.getAccountDiscriminator("Position");

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

        positions.sort(Comparator.comparingDouble(JupiterPerpPosition::getSizeUsd).reversed());

        // Log the positions
        for (JupiterPerpPosition position : positions) {
            double leverage = (double) position.getSizeUsd() / position.getCollateralUsd();
            // log.info("Owner: {}, Size USD: {}, Leverage: {}", position.getOwner().toBase58(), position.getSizeUsd(), leverage);
        }

        for (int i = 0; i < 4; i++) {
            JupiterPerpPosition position = positions.get(i);
            double leverage = position.getSizeUsd() / position.getCollateralUsd();

            log.info(
                    String.format(
                            "Position #%d: Owner[%s], Size[$%.2f], Entry[$%.2f] Leverage: %.2fx",
                            i + 1,
                            position.getOwner().toBase58().substring(0, 5).concat("..."),
                            position.getSizeUsd(),
                            position.getPrice(),
                            leverage
                    )
            );
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
    @Disabled
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

    // Disabled since it relies on hardcoded accounts (and positions always close)
    @Test
    @Disabled
    public void testJupiterManager() {
        JupiterManager manager = new JupiterManager(client);

        // Test getPosition
        PublicKey positionPublicKey = new PublicKey("FdqbJAvADUJzZsBFK1ArhV79vXLmpKUMB4oXSrW8rSE");
        Optional<JupiterPerpPosition> position = manager.getPosition(positionPublicKey);
        assertTrue(position.isPresent());
        assertEquals(new PublicKey("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq"), position.get().getOwner());

        // Test getPool
        PublicKey poolPublicKey = new PublicKey("5BUwFW4nRbftYTDMbgxykoFWqWHPzahFSNAaaaJtVKsq");
        Optional<JupiterPool> pool = manager.getPool(poolPublicKey);
        assertTrue(pool.isPresent());
        assertEquals("Pool", pool.get().getName());

        // Test getCustody
        PublicKey custodyPublicKey = new PublicKey("7xS2gz2bTp3fwCC7knJvUWTEU9Tycczu6VhJYKgi1wdz");
        Optional<JupiterCustody> custody = manager.getCustody(custodyPublicKey);
        assertTrue(custody.isPresent());
        assertNotNull(custody.get().getPool());

        // Test getPositionRequest
        PublicKey positionRequestPublicKey = new PublicKey("APYrGNtsTTMpNNBQBpALxYnwYfKDQyFocf3d1j6jkuzf");
        Optional<JupiterPositionRequest> positionRequest = manager.getPositionRequest(positionRequestPublicKey);
        assertTrue(positionRequest.isPresent());
        assertNotNull(positionRequest.get().getOwner());

        // Test getPerpetuals
        PublicKey perpetualsPublicKey = new PublicKey("H4ND9aYttUVLFmNypZqLjZ52FYiGvdEB45GmwNoKEjTj");
        Optional<JupiterPerpetuals> perpetuals = manager.getPerpetuals(perpetualsPublicKey);
        assertTrue(perpetuals.isPresent());
        assertNotNull(perpetuals.get().getPool());
    }

    @Test
    public void testJupiterManagerWithInvalidPublicKeys() {
        JupiterManager manager = new JupiterManager(client);
        PublicKey invalidPublicKey = new PublicKey("1111111111111111111111111111111111111111111");

        // Test all methods with invalid public key
        assertFalse(manager.getPosition(invalidPublicKey).isPresent());
        assertFalse(manager.getPool(invalidPublicKey).isPresent());
        assertFalse(manager.getCustody(invalidPublicKey).isPresent());
        assertFalse(manager.getPositionRequest(invalidPublicKey).isPresent());
        assertFalse(manager.getPerpetuals(invalidPublicKey).isPresent());
    }

    @Test
    public void testGetAllJupiterDcaAccounts() {
        JupiterManager manager = new JupiterManager(client);

        List<JupiterDca> dcaAccounts = manager.getDcaAccounts();
        assertNotNull(dcaAccounts, "DCA accounts list should not be null");
        assertTrue(dcaAccounts.size() > 0, "DCA accounts list should contain at least one account");

        for (JupiterDca dca : dcaAccounts) {
            assertNotNull(dca.getUser(), "DCA user should not be null");
            assertNotNull(dca.getInputMint(), "DCA inputMint should not be null");
            assertNotNull(dca.getOutputMint(), "DCA outputMint should not be null");
            assertTrue(dca.getNextCycleAt() > 0, "DCA nextCycleAt should be greater than 0");
            assertTrue(dca.getOutWithdrawn() >= 0, "DCA outWithdrawn should be non-negative");
            assertTrue(dca.getInUsed() >= 0, "DCA inUsed should be non-negative");
            assertTrue(dca.getOutReceived() >= 0, "DCA outReceived should be non-negative");
            assertTrue(dca.getInAmountPerCycle() > 0, "DCA inAmountPerCycle should be greater than 0");
            assertTrue(dca.getCycleFrequency() > 0, "DCA cycleFrequency should be greater than 0");
            assertTrue(dca.getNextCycleAmountLeft() >= 0, "DCA nextCycleAmountLeft should be non-negative");
            assertNotNull(dca.getInAccount(), "DCA inAccount should not be null");
            assertNotNull(dca.getOutAccount(), "DCA outAccount should not be null");
            assertTrue(dca.getCreatedAt() > 0, "DCA createdAt should be greater than 0");
        }

        // Log the retrieved DCA accounts
        for (JupiterDca dca : dcaAccounts) {
            log.info("JupiterDca: {}", dca);
        }

    }

    @Test
    public void getMostRecentJupiterDcaAccounts() {
        JupiterManager manager = new JupiterManager(client);
        List<JupiterDca> dcaAccounts = manager.getDcaAccounts();
        dcaAccounts.sort(Comparator.comparingLong(JupiterDca::getCreatedAt).reversed());

        for (int i = 0; i < 10; i++) {
            JupiterDca dca = dcaAccounts.get(i);
            log.info("DCA {} #{}: {}", Instant.ofEpochSecond(dca.getCreatedAt()),i + 1, dca);
        }

        log.info("Only showing ones where nextCycleAt > createdAt && nextCycleAt > now && inUsed < inDeposited");
        List<JupiterDca> activeDcas = dcaAccounts.stream()
                .filter(dca -> dca.getNextCycleAt() > dca.getCreatedAt() && dca.getNextCycleAt() > Instant.now().getEpochSecond())
                .filter(dca -> dca.getInUsed() < dca.getInDeposited())
                .toList();

        activeDcas.forEach(dca -> log.info("DCA {} #{}: {}", Instant.ofEpochSecond(dca.getCreatedAt()), dcaAccounts.indexOf(dca) + 1, dca));
        log.info("Size: {}", activeDcas.size());
    }

    @Test
    public void testGetOpenDcaOrders() {
        JupiterManager manager = new JupiterManager(client);
        
        // Mocking price data for tokens (Replace with actual price retrieval in production)
        Map<String, Double> tokenPrices = new HashMap<>();
        tokenPrices.put("Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB", 1.0); // Example price for inputMint
        tokenPrices.put("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v", 1.0); // Example price for another inputMint
        tokenPrices.put("So11111111111111111111111111111111111111112", 147.0); // Example price for inputMint
        tokenPrices.put("DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263", 0.00002479); // Example price for outputMint
        // Add more token prices as needed

        List<JupiterDca> dcaAccounts = manager.getDcaAccounts();
        assertNotNull(dcaAccounts, "DCA accounts list should not be null");
        assertFalse(dcaAccounts.isEmpty(), "DCA accounts list should contain at least one account");

        long now = Instant.now().getEpochSecond();

        List<JupiterDca> openDcaOrders = dcaAccounts.stream()
            // Filter where nextCycleAt > createdAt and nextCycleAt > now and inUsed < inDeposited
            .filter(dca -> dca.getNextCycleAt() > dca.getCreatedAt()
                        && dca.getNextCycleAt() > now
                        && dca.getInUsed() < dca.getInDeposited())
            // Filter where percent_remaining > 0
            .filter(dca -> {
                double remainingInputAmount = (double) (dca.getInDeposited() - dca.getInUsed());
                double inputAmount = (double) dca.getInDeposited() / Math.pow(10, 6); // Assuming 6 decimals
                double percentRemaining = 100 * remainingInputAmount / inputAmount;
                return percentRemaining > 0;
            })
            // Filter based on is_in_range constraints
            .filter(dca -> {
                double inputOrderSize = (double) dca.getInAmountPerCycle() / Math.pow(10, 6); // Assuming 6 decimals

                // Retrieve prices; default to 1.0 if not found
                double inputPriceUsd = tokenPrices.getOrDefault(dca.getInputMint().toBase58(), 1.0);
                double outputPriceUsd = tokenPrices.getOrDefault(dca.getOutputMint().toBase58(), 1.0);

                double outputOrderSize = inputOrderSize * inputPriceUsd / outputPriceUsd;

                boolean minInRange = (dca.getMinOutAmount() == 0) 
                                     || (outputOrderSize >= ((double) dca.getMinOutAmount() / Math.pow(10, 6))); // Assuming 6 decimals
                boolean maxInRange = (dca.getMaxOutAmount() == 0) 
                                     || (outputOrderSize <= ((double) dca.getMaxOutAmount() / Math.pow(10, 6))); // Assuming 6 decimals

                return minInRange && maxInRange;
            })
            .collect(Collectors.toList());

        assertNotNull(openDcaOrders, "Open DCA orders list should not be null");

        // Assertions to ensure all open DCA orders meet the criteria
        openDcaOrders.forEach(dca -> {
            // Verify nextCycleAt constraints
            assertTrue(dca.getNextCycleAt() > dca.getCreatedAt(), "nextCycleAt should be greater than createdAt");
            assertTrue(dca.getNextCycleAt() > now, "nextCycleAt should be in the future");
            assertTrue(dca.getInUsed() < dca.getInDeposited(), "inUsed should be less than inDeposited");

            // Verify percent_remaining > 0
            double remainingInputAmount = (double) (dca.getInDeposited() - dca.getInUsed());
            double inputAmount = (double) dca.getInDeposited() / Math.pow(10, 6); // Assuming 6 decimals
            double percentRemaining = 100 * remainingInputAmount / inputAmount;
            assertTrue(percentRemaining > 0, "Percent remaining should be greater than 0");

            // Verify is_in_range constraints
            double inputOrderSize = (double) dca.getInAmountPerCycle() / Math.pow(10, 6); // Assuming 6 decimals
            double inputPriceUsd = tokenPrices.getOrDefault(dca.getInputMint().toBase58(), 1.0);
            double outputPriceUsd = tokenPrices.getOrDefault(dca.getOutputMint().toBase58(), 1.0);
            double outputOrderSize = inputOrderSize * inputPriceUsd / outputPriceUsd;

            boolean minInRange = (dca.getMinOutAmount() == 0) 
                                 || (outputOrderSize >= ((double) dca.getMinOutAmount() / Math.pow(10, 6))); // Assuming 6 decimals
            boolean maxInRange = (dca.getMaxOutAmount() == 0) 
                                 || (outputOrderSize <= ((double) dca.getMaxOutAmount() / Math.pow(10, 6))); // Assuming 6 decimals

            assertTrue(minInRange, "Output order size should be within the minimum range");
            assertTrue(maxInRange, "Output order size should be within the maximum range");
        });

        log.info("Open DCA Orders [{}]: {}", openDcaOrders.size(), openDcaOrders);
        log.info("Size: {}", openDcaOrders.size());
    }

    @Test
    public void testGetDcaAccountsByUserFiltering() {
        JupiterManager manager = new JupiterManager(client);
        List<JupiterDca> jupiterDcas = manager.getDcaAccounts()
                .stream()
                .filter(jupiterDca -> jupiterDca.getUser().equals(new PublicKey("ESmavfhN3JKy3q3iJfP2FJYWNDRWVEkcKmzzVfetU5eB")))
                .toList();
        log.info("DCAs for ESmavfhN3JKy3q3iJfP2FJYWNDRWVEkcKmzzVfetU5eB: {}", jupiterDcas);
    }

    @Test
    public void testGetDcaAccountsByUser() {
        JupiterManager manager = new JupiterManager(client);
        List<JupiterDca> jupiterDcas = manager.getDcaAccounts(new PublicKey("ESmavfhN3JKy3q3iJfP2FJYWNDRWVEkcKmzzVfetU5eB"));
        log.info("DCAs for user: {}", jupiterDcas);
    }
}