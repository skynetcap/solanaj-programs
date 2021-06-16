package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.p2p.solanaj.core.PublicKey;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
public class MangoGroup {
    // Constants
    private static final int NUM_TOKENS = 5;

    // Offsets
    private static final int TOKENS_OFFSET = 8;
    private static final int VAULTS_OFFSET = TOKENS_OFFSET + (32 * NUM_TOKENS);
    private static final int INDEXES_OFFSET = VAULTS_OFFSET + (32 * NUM_TOKENS);

    private MangoGroupAccountFlags accountFlags;
    private List<PublicKey> tokens;
    private List<PublicKey> vaults;

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
            PublicKey tokenPubkey = PublicKey.readPubkey(data, TOKENS_OFFSET + (i  * 32));
            mangoGroup.getTokens().add(tokenPubkey);
        }

        // Listed vaults
        mangoGroup.setVaults(new ArrayList<>());
        for (int i = 0; i < NUM_TOKENS; i++) {
            PublicKey vaultPubkey = PublicKey.readPubkey(data, VAULTS_OFFSET + (i  * 32));
            mangoGroup.getVaults().add(vaultPubkey);
        }

        return mangoGroup;
    }
}
