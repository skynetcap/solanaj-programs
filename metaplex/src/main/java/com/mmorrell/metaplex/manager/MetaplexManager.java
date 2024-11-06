package com.mmorrell.metaplex.manager;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mmorrell.metaplex.model.Metadata;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;

import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.utils.ByteUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MetaplexManager {

    private final RpcClient client;
    private final Map<PublicKey, Metadata> metadataCache = new HashMap<>();
    private static final PublicKey METAPLEX = new PublicKey("metaqbxxUerdq28cj1RbAWkYQm3ybzjb6a8bt518x1s");

    // Offsets (from https://docs.metaplex.com/programs/token-metadata/accounts)
    private static final int UPDATE_AUTHORITY_OFFSET = 1;
    private static final int MINT_OFFSET = 33;
    private static final int NAME_OFFSET = 65;
    private static final int SYMBOL_OFFSET = 101;
    private static final int URI_OFFSET = 119;

    // Sizes (in bytes)
    private static final int NAME_SIZE = 36;
    private static final int SYMBOL_SIZE = 14;
    private static final int URI_SIZE = 204;

    public MetaplexManager(final RpcClient client) {
        this.client = client;
    }

    public Optional<Metadata> getTokenMetadata(final PublicKey tokenMint) {
        if (metadataCache.containsKey(tokenMint)) {
            return Optional.of(metadataCache.get(tokenMint));
        }

        try {
            final PublicKey.ProgramDerivedAddress metadataPda = PublicKey.findProgramAddress(
                    List.of(
                            "metadata".getBytes(StandardCharsets.UTF_8),
                            METAPLEX.toByteArray(),
                            tokenMint.toByteArray()
                    ),
                    METAPLEX
            );

            final AccountInfo accountInfo = client.getApi().getAccountInfo(metadataPda.getAddress());
            byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));

            final Metadata metadata = Metadata.builder()
                    .updateAuthority(PublicKey.readPubkey(data, UPDATE_AUTHORITY_OFFSET))
                    .tokenMint(PublicKey.readPubkey(data, MINT_OFFSET))
                    .name(new String(ByteUtils.readBytes(data, NAME_OFFSET, NAME_SIZE)).trim())
                    .symbol(new String(ByteUtils.readBytes(data, SYMBOL_OFFSET, SYMBOL_SIZE)).trim())
                    .uri(new String(ByteUtils.readBytes(data, URI_OFFSET, URI_SIZE)).trim())
                    .build();

            metadataCache.put(tokenMint, metadata);
            return Optional.ofNullable(metadata);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Map<PublicKey, Metadata> getTokenMetadata(final List<PublicKey> tokenMints) {
        final Map<PublicKey, Metadata> metadataMap = new HashMap<>();
        final Map<PublicKey, PublicKey> metadataToTokenMintMap = new HashMap<>();
        final int batchSize = 100;

        // Split tokenMints into batches of 100
        List<List<PublicKey>> batches = new ArrayList<>();
        for (int i = 0; i < tokenMints.size(); i += batchSize) {
            int end = Math.min(i + batchSize, tokenMints.size());
            batches.add(tokenMints.subList(i, end));
        }

        for (List<PublicKey> batch : batches) {
            List<PublicKey> metadataPdas = batch.stream()
                    .map(tokenMint -> {
                        PublicKey metadataPda = PublicKey.findProgramAddress(
                                List.of(
                                        "metadata".getBytes(StandardCharsets.UTF_8),
                                        METAPLEX.toByteArray(),
                                        tokenMint.toByteArray()
                                ),
                                METAPLEX
                        ).getAddress();
                        metadataToTokenMintMap.put(metadataPda, tokenMint);
                        return metadataPda;
                    })
                    .collect(Collectors.toList());

            Map<PublicKey, Optional<AccountInfo.Value>> accountInfos = Collections.emptyMap();

            try {
                accountInfos = client.getApi().getMultipleAccountsMap(metadataPdas);
            } catch (RpcException e) {
                log.info("Error getting multiple accounts map for batch starting at index {}: {}",
                        tokenMints.indexOf(batch.get(0)), e.getMessage());
                continue; // Skip this batch and proceed with the next
            }

            // Map to track lookups we have to do
            Map<PublicKey, String> tokenMintJsonMap = new HashMap<>();

            for (Map.Entry<PublicKey, Optional<AccountInfo.Value>> entry : accountInfos.entrySet()) {
                PublicKey metadataPda = entry.getKey();
                Optional<AccountInfo.Value> accountInfoOpt = entry.getValue();

                if (metadataCache.containsKey(metadataToTokenMintMap.get(metadataPda))) {
                    metadataMap.put(metadataToTokenMintMap.get(metadataPda), metadataCache.get(metadataToTokenMintMap.get(metadataPda)));
                } else if (accountInfoOpt.isEmpty()) {
                    // No data available for this PDA
                } else {
                    try {
                        byte[] data = Base64.getDecoder().decode(accountInfoOpt.get().getData().get(0));
                        Metadata metadata = Metadata.builder()
                                .updateAuthority(PublicKey.readPubkey(data, UPDATE_AUTHORITY_OFFSET))
                                .tokenMint(PublicKey.readPubkey(data, MINT_OFFSET))
                                .name(new String(ByteUtils.readBytes(data, NAME_OFFSET, NAME_SIZE)).trim())
                                .symbol(new String(ByteUtils.readBytes(data, SYMBOL_OFFSET, SYMBOL_SIZE)).trim())
                                .uri(new String(ByteUtils.readBytes(data, URI_OFFSET, URI_SIZE)).trim())
                                .build();

                        tokenMintJsonMap.put(metadataToTokenMintMap.get(metadataPda), metadata.getUri());
                        metadataCache.put(metadataToTokenMintMap.get(metadataPda), metadata);
                        metadataMap.put(metadataToTokenMintMap.get(metadataPda), metadata);
                    } catch (Exception e) {
                        log.error("Error parsing metadata for PDA {}: {}", metadataPda, e.getMessage());
                    }
                }
            }
            // iterate tokenMintJsonMap todo
        }

        return metadataMap;
    }
}