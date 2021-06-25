package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class MangoAccountMetadata {

    public static final int METADATA_LAYOUT_SIZE = 8;

    private byte dataType;
    private byte version;
    private boolean isInitialized;

    public static MangoAccountMetadata readMangoAccountMetadata(byte[] data) {
        return MangoAccountMetadata.builder()
                .dataType(data[0])
                .version(data[1])
                .isInitialized(data[2] == 1)
                .build();
    }
}
