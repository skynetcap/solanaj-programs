package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.utils.ByteUtils;

import java.util.Arrays;

/**
 * Represents a v3 Mango perp account
 */

@Builder
@Getter
@Setter
@ToString
public class MangoPerpGroup {

    private static final int METADATA_OFFSET = 0;
    private static final int NUM_ORACLES_OFFSET = METADATA_OFFSET + MangoAccountMetadata.METADATA_LAYOUT_SIZE;

    private PublicKey publicKey;
    private MangoAccountMetadata metadata;
    private long numOracles;

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

        return mangoPerpGroup;
    }

}
