package com.mmorrell.metaplex.manager;

import com.mmorrell.metaplex.model.Metadata;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.utils.ByteUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class MetaplexManager {

    private final RpcClient client;
    private static final Logger LOGGER = Logger.getLogger(MetaplexManager.class.getName());
    private static final PublicKey METAPLEX = new PublicKey("metaqbxxUerdq28cj1RbAWkYQm3ybzjb6a8bt518x1s");

    // Offsets (from https://docs.metaplex.com/programs/token-metadata/accounts)
    private static final int UPDATE_AUTHORITY_OFFSET = 1;
    private static final int MINT_OFFSET = 33;
    private static final int NAME_OFFSET = 65;
    private static final int SYMBOL_OFFSET = 101;
    private static final int URI_OFFSET = 115;

    // Sizes (in bytes)
    private static final int NAME_SIZE = 36;
    private static final int SYMBOL_SIZE = 14;
    private static final int URI_SIZE = 204;

    public MetaplexManager(final RpcClient client) {
        this.client = client;
    }

    public Optional<Metadata> getTokenMetadata(final PublicKey tokenMint) {
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

            return Optional.ofNullable(metadata);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
