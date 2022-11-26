package com.mmorrell.zeta.model;

import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;

@Data
@Builder
public class ZetaExpiry {

    private long activeTs;
    private long expiryTs;
    private boolean dirty;
    // Plus 15 bytes of padding. == 32 byte length

    public static ZetaExpiry readZetaExpiry(byte[] data) {
        return ZetaExpiry.builder()
                .activeTs(Utils.readInt64(data, 0))
                .expiryTs(Utils.readInt64(data, 8))
                .dirty(data[16] == 1)
                .build();
    }
}
