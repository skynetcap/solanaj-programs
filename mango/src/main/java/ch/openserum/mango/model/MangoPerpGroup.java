package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.utils.ByteUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ch.openserum.mango.model.MangoUtils.U64_SIZE_BYTES;

/**
 * Represents a v3 Mango Perp group
 */

@Builder
@Getter
@Setter
@ToString
public class MangoPerpGroup {

    // Constants
    private static final int MAX_TOKENS = 32;

    // Offsets
    private static final int METADATA_OFFSET = 0;
    private static final int NUM_ORACLES_OFFSET = METADATA_OFFSET + MangoAccountMetadata.METADATA_LAYOUT_SIZE;
    private static final int TOKEN_INFO_LAYOUT_OFFSET = NUM_ORACLES_OFFSET + U64_SIZE_BYTES;

    private PublicKey publicKey;
    private MangoAccountMetadata metadata;
    private long numOracles;
    private List<MangoTokenInfo> tokens;

    public static MangoPerpGroup readMangoPerpGroup(final PublicKey publicKey, byte[] data) {
        final MangoPerpGroup mangoPerpGroup = MangoPerpGroup.builder()
                .publicKey(publicKey)
                .build();

        mangoPerpGroup.setMetadata(
                MangoAccountMetadata.readMangoAccountMetadata(
                        Arrays.copyOfRange(data, METADATA_OFFSET, MangoAccountMetadata.METADATA_LAYOUT_SIZE)
                )
        );

        mangoPerpGroup.setNumOracles(
                ByteUtils.readUint64(data, NUM_ORACLES_OFFSET).longValue()
        );

        mangoPerpGroup.setTokens(new ArrayList<>());
        for (int i = 0; i < MAX_TOKENS; i++) {
            final MangoTokenInfo mangoTokenInfo = MangoTokenInfo.readMangoTokenInfo(
                    Arrays.copyOfRange(
                            data,
                            TOKEN_INFO_LAYOUT_OFFSET + (i * MangoTokenInfo.MANGO_TOKEN_INFO_LAYOUT_SIZE),
                            TOKEN_INFO_LAYOUT_OFFSET + (i * MangoTokenInfo.MANGO_TOKEN_INFO_LAYOUT_SIZE)
                                    + MangoTokenInfo.MANGO_TOKEN_INFO_LAYOUT_SIZE
                    )
            );

            if (mangoTokenInfo.getDecimals() != 0) {
                mangoPerpGroup.getTokens().add(mangoTokenInfo);
            }
        }

        return mangoPerpGroup;
    }

}
