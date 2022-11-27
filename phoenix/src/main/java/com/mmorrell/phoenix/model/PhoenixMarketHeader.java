package com.mmorrell.phoenix.model;

import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;

@Data
@Builder
public class PhoenixMarketHeader {

    private long discriminant;
    private long status;

    public static PhoenixMarketHeader readPhoenixMarketHeader(byte[] data) {
        return PhoenixMarketHeader.builder()
                .discriminant(Utils.readInt64(data, 0))
                .status(Utils.readInt64(data, 8))
                .build();
    }

}
