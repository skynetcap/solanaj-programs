package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;

import java.util.Arrays;

/**
 * Represents a v3 Mango perp account
 */

@Builder
@Getter
@Setter
@ToString
public class MangoAccount {

    private static final int METADATA_OFFSET = 0;

    private PublicKey publicKey;
    private MangoAccountMetadata metadata;

    public static MangoAccount readMangoAccount(final PublicKey publicKey, byte[] data) {
        final MangoAccount mangoAccount = MangoAccount.builder()
                .publicKey(publicKey)
                .build();

        mangoAccount.setMetadata(
                MangoAccountMetadata.readMangoAccountMetadata(
                        Arrays.copyOfRange(data, METADATA_OFFSET, MangoAccountMetadata.METADATA_LAYOUT_SIZE)
                )
        );

        return mangoAccount;
    }

}
