package com.mmorrell.jupiter.manager;

import com.mmorrell.jupiter.model.*;
import com.mmorrell.jupiter.util.JupiterUtil;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.Memcmp;
import org.p2p.solanaj.rpc.types.ProgramAccount;

import java.util.*;
import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
public class JupiterManager {

    private final RpcClient client;
    private static final PublicKey JUPITER_PROGRAM_ID = new PublicKey("PERPHjGBqRHArX4DySjwM6UJHiR3sWAatqfdBS2qQJu");
    private static final PublicKey DCA_PROGRAM_ID = new PublicKey("DCA265Vj8a9CEuX1eb1LWRnDT7uK6q1xMipnNyatn23M");
    private static final int DCA_ACCOUNT_SIZE = 289; // Updated based on JupiterDca structure

    public JupiterManager() {
        this.client = new RpcClient(Cluster.MAINNET);
    }


    public JupiterManager(RpcClient client) {
        this.client = client;
    }

    public Optional<JupiterPerpPosition> getPosition(PublicKey positionPublicKey) {
        try {
            AccountInfo accountInfo = client.getApi().getAccountInfo(positionPublicKey);
            if (accountInfo == null || accountInfo.getValue() == null) {
                return Optional.empty();
            }
            byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
            return Optional.of(JupiterPerpPosition.fromByteArray(data));
        } catch (RpcException e) {
            log.warn("Error fetching position: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<JupiterPool> getPool(PublicKey poolPublicKey) {
        try {
            AccountInfo accountInfo = client.getApi().getAccountInfo(poolPublicKey);
            if (accountInfo == null || accountInfo.getValue() == null) {
                return Optional.empty();
            }
            byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
            return Optional.of(JupiterPool.fromByteArray(data));
        } catch (RpcException e) {
            log.warn("Error fetching pool: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<JupiterCustody> getCustody(PublicKey custodyPublicKey) {
        try {
            AccountInfo accountInfo = client.getApi().getAccountInfo(custodyPublicKey);
            if (accountInfo == null || accountInfo.getValue() == null) {
                return Optional.empty();
            }
            byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
            return Optional.of(JupiterCustody.fromByteArray(data));
        } catch (RpcException e) {
            log.warn("Error fetching custody: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<JupiterPositionRequest> getPositionRequest(PublicKey positionRequestPublicKey) {
        try {
            AccountInfo accountInfo = client.getApi().getAccountInfo(positionRequestPublicKey);
            if (accountInfo == null || accountInfo.getValue() == null) {
                return Optional.empty();
            }
            byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
            return Optional.of(JupiterPositionRequest.fromByteArray(data));
        } catch (RpcException e) {
            log.warn("Error fetching position request: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<JupiterPerpetuals> getPerpetuals(PublicKey perpetualsPublicKey) {
        try {
            AccountInfo accountInfo = client.getApi().getAccountInfo(perpetualsPublicKey);
            if (accountInfo == null || accountInfo.getValue() == null) {
                return Optional.empty();
            }
            byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
            return Optional.of(JupiterPerpetuals.fromByteArray(data));
        } catch (RpcException e) {
            log.warn("Error fetching perpetuals: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves all Jupiter DCA accounts.
     *
     * @return a list of JupiterDca objects.
     * @throws RpcException if the RPC call fails.
     */
    public List<JupiterDca> getDcaAccounts() {
        byte[] dcaDiscriminator = JupiterUtil.getAccountDiscriminator("Dca");

        // Create a memcmp filter for the discriminator at offset 0
        Memcmp memCmpFilter = new Memcmp(0, Base58.encode(dcaDiscriminator));

        try {
            List<ProgramAccount> accounts = client.getApi().getProgramAccounts(
                    DCA_PROGRAM_ID,
                    List.of(memCmpFilter),
                    DCA_ACCOUNT_SIZE
            );

            List<JupiterDca> dcaAccounts = new ArrayList<>();
            for (ProgramAccount account : accounts) {
                byte[] data = account.getAccount().getDecodedData();
                JupiterDca dca = JupiterDca.fromByteArray(data);
                dca.setPublicKey(account.getPublicKey());
                dcaAccounts.add(dca);
            }

            return dcaAccounts;
        } catch (RpcException ex) {
            log.warn("Error fetching DCA accounts: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public List<JupiterDca> getDcaAccounts(PublicKey user) {
        byte[] dcaDiscriminator = JupiterUtil.getAccountDiscriminator("Dca");

        // Create a memcmp filter for the discriminator at offset 0
        Memcmp memCmpFilter = new Memcmp(0, Base58.encode(dcaDiscriminator));
        Memcmp memCmpFilterUser = new Memcmp(8, Base58.encode(user.toByteArray()));

        try {
            List<ProgramAccount> accounts = client.getApi().getProgramAccounts(
                    DCA_PROGRAM_ID,
                    List.of(memCmpFilter, memCmpFilterUser),
                    DCA_ACCOUNT_SIZE
            );

            List<JupiterDca> dcaAccounts = new ArrayList<>();
            for (ProgramAccount account : accounts) {
                byte[] data = account.getAccount().getDecodedData();
                JupiterDca dca = JupiterDca.fromByteArray(data);
                dca.setPublicKey(account.getPublicKey());
                dcaAccounts.add(dca);
            }

            return dcaAccounts;
        } catch (RpcException ex) {
            log.warn("Error fetching DCA accounts: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    public List<JupiterDca> getActiveDcaOrders() {
        return getDcaAccounts().stream()
            .filter(dca -> dca.getInUsed() <= dca.getInDeposited())
            .collect(Collectors.toList());
    }

    public List<JupiterDca> getDcaOrdersByTokenPair(PublicKey inputMint, PublicKey outputMint) {
        return getDcaAccounts().stream()
            .filter(dca -> dca.getInputMint().equals(inputMint) && dca.getOutputMint().equals(outputMint))
            .collect(Collectors.toList());
    }

    public double getAggregatedDcaVolume(PublicKey inputMint, PublicKey outputMint, long startTime, long endTime) {
        return getDcaAccounts().stream()
            .filter(dca -> dca.getInputMint().equals(inputMint) 
                        && dca.getOutputMint().equals(outputMint)
                        && dca.getCreatedAt() >= startTime
                        && dca.getCreatedAt() <= endTime)
            .mapToDouble(dca -> (double) dca.getInDeposited() / Math.pow(10, 6))
            .sum();
    }

    public List<Map.Entry<Map.Entry<PublicKey, PublicKey>, Long>> getMostPopularDcaPairs(int limit) {
        Map<Map.Entry<PublicKey, PublicKey>, Long> pairCounts = getDcaAccounts().stream()
            .collect(Collectors.groupingBy(
                dca -> new AbstractMap.SimpleEntry<>(dca.getInputMint(), dca.getOutputMint()),
                Collectors.counting()
            ));
        
        return pairCounts.entrySet().stream()
            .sorted(Map.Entry.<Map.Entry<PublicKey, PublicKey>, Long>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves completed Jupiter DCA orders.
     * A DCA order is considered completed if it has fully utilized its deposited amount or has expired.
     *
     * @return a list of completed JupiterDca objects.
     */
    public List<JupiterDca> getCompletedDcaOrders() {
        long now = Instant.now().getEpochSecond();
        return getDcaAccounts().stream()
                .filter(dca -> dca.getInUsed() >= dca.getInDeposited() || dca.getNextCycleAt() <= now)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves Jupiter DCA orders created within a specific time range.
     *
     * @param startTime the start epoch time.
     * @param endTime   the end epoch time.
     * @return a list of JupiterDca objects within the specified time range.
     */
    public List<JupiterDca> getDcaOrdersByTimeRange(long startTime, long endTime) {
        return getDcaAccounts().stream()
                .filter(dca -> dca.getCreatedAt() >= startTime && dca.getCreatedAt() <= endTime)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves Jupiter DCA orders sorted by the total deposited amount in descending order.
     *
     * @return a list of JupiterDca objects sorted by volume.
     */
    public List<JupiterDca> getDcaOrdersSortedByVolume() {
        return getDcaAccounts().stream()
                .sorted(Comparator.comparingLong(JupiterDca::getInDeposited).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves aggregated statistics for a specific user's DCA activities.
     *
     * @param user the PublicKey of the user.
     * @return a UserDcaStats object containing aggregated statistics.
     */
    public JupiterUserDcaStats getUserDcaStatistics(PublicKey user) {
        List<JupiterDca> userDcas = getDcaAccounts(user);

        long totalOrders = userDcas.size();
        double totalVolume = userDcas.stream()
                .mapToDouble(dca -> (double) dca.getInDeposited() / Math.pow(10, 6))
                .sum();
        Set<PublicKey> uniqueInputTokens = userDcas.stream()
                .map(JupiterDca::getInputMint)
                .collect(Collectors.toSet());
        Set<PublicKey> uniqueOutputTokens = userDcas.stream()
                .map(JupiterDca::getOutputMint)
                .collect(Collectors.toSet());

        return new JupiterUserDcaStats(totalOrders, totalVolume, uniqueInputTokens, uniqueOutputTokens);
    }


    /**
     * Retrieves the most recent Jupiter DCA orders up to the specified limit.
     *
     * @param limit the maximum number of recent orders to retrieve.
     * @return a list of recent JupiterDca objects.
     */
    public List<JupiterDca> getRecentDcaOrders(int limit) {
        return getDcaAccounts().stream()
                .sorted(Comparator.comparingLong(JupiterDca::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Additional methods can be added here as needed for other use cases.
}