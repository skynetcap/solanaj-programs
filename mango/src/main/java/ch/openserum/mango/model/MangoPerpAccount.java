package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;

import java.util.Arrays;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
public class MangoPerpAccount {

    // Constants
    private static final int MAX_TOKENS = 32;
    private static final int MAX_PAIRS = MAX_TOKENS - 1;

    // Offsets
    private static final int METADATA_OFFSET = 0;
    private static final int MANGO_GROUP_OFFSET = METADATA_OFFSET + MangoAccountMetadata.METADATA_LAYOUT_SIZE;
    private static final int OWNER_OFFSET = MANGO_GROUP_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;

    private PublicKey publicKey;
    private MangoAccountMetadata metaData;
    private PublicKey mangoGroup;
    private PublicKey owner;
    private List<Boolean> inMarginBasket;

    public static MangoPerpAccount readMangoPerpAccount(final PublicKey publicKey, byte[] data) {
        final MangoPerpAccount mangoPerpAccount = MangoPerpAccount.builder()
                .publicKey(publicKey)
                .metaData(
                        MangoAccountMetadata.readMangoAccountMetadata(
                                Arrays.copyOfRange(
                                        data,
                                        METADATA_OFFSET,
                                        METADATA_OFFSET + MangoAccountMetadata.METADATA_LAYOUT_SIZE
                                )
                        )
                )
                .mangoGroup(PublicKey.readPubkey(data, MANGO_GROUP_OFFSET))
                .owner(PublicKey.readPubkey(data, OWNER_OFFSET))
                .build();

        return mangoPerpAccount;
    }
}
