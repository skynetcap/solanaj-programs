package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ch.openserum.mango.model.MangoUtils.*;

@Builder
@Getter
@Setter
public class MangoGroup {
    // Constants
    private static final int NUM_TOKENS = 5;

    // Offsets
    private static final int TOKENS_OFFSET = ACCOUNT_FLAGS_SIZE_BYTES;
    private static final int VAULTS_OFFSET = TOKENS_OFFSET + (32 * NUM_TOKENS);
    private static final int INDEXES_OFFSET = VAULTS_OFFSET + (32 * NUM_TOKENS);

    private MangoGroupAccountFlags accountFlags;
    private List<PublicKey> tokens;
    private List<PublicKey> vaults;
    private List<MangoIndex> indexes;

    public static MangoGroup readMangoGroup(byte[] data) {
        // Mango groups only store 4 booleans currently, 1 byte is enough
        byte mangoGroupAccountFlags = data[0];

        // Mango Group account flags
        final MangoGroup mangoGroup = MangoGroup.builder()
                .accountFlags(
                        MangoGroupAccountFlags.builder()
                                .initialized((mangoGroupAccountFlags & 1) == 1)
                                .mangoGroup((mangoGroupAccountFlags & 2) == 2)
                                .marginAccount((mangoGroupAccountFlags & 4) == 4)
                                .mangoSrmAccount((mangoGroupAccountFlags & 8) == 8)
                                .build()
                )
                .build();

        // Listed tokens
        mangoGroup.setTokens(new ArrayList<>());
        for (int i = 0; i < NUM_TOKENS; i++) {
            final PublicKey tokenPubkey = PublicKey.readPubkey(data, TOKENS_OFFSET + (i  * 32));
            mangoGroup.getTokens().add(tokenPubkey);
        }

        // Listed vaults
        mangoGroup.setVaults(new ArrayList<>());
        for (int i = 0; i < NUM_TOKENS; i++) {
            final PublicKey vaultPubkey = PublicKey.readPubkey(data, VAULTS_OFFSET + (i  * 32));
            mangoGroup.getVaults().add(vaultPubkey);
        }

        // Indexes
        mangoGroup.setIndexes(new ArrayList<>());
        for (int i = 0; i < NUM_TOKENS; i++) {
            int counter = (i * (U64_SIZE_BYTES + (2 * U64F64_SIZE_BYTES))); // (i * 40)
            long lastUpdate = Utils.readInt64(data, INDEXES_OFFSET);

            byte[] borrow = Arrays.copyOfRange(
                    data,
                    INDEXES_OFFSET + U64_SIZE_BYTES + counter,
                    INDEXES_OFFSET + U64_SIZE_BYTES + U64F64_SIZE_BYTES + counter
            );

            byte[] deposit = Arrays.copyOfRange(
                    data,
                    INDEXES_OFFSET + U64_SIZE_BYTES + U64F64_SIZE_BYTES + counter,
                    INDEXES_OFFSET + U64_SIZE_BYTES + U64F64_SIZE_BYTES + U64F64_SIZE_BYTES + counter
            );

            final MangoIndex mangoIndex = MangoIndex.builder()
                    .lastUpdate(lastUpdate)
                    .borrow(borrow)
                    .deposit(deposit)
                    .build();

            mangoGroup.getIndexes().add(mangoIndex);
        }

        return mangoGroup;
    }
}
