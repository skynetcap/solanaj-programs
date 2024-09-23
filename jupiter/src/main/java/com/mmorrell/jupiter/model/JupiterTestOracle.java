package com.mmorrell.jupiter.model;

import com.mmorrell.jupiter.util.JupiterUtil;
import com.mmorrell.openbook.OpenBookUtil;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a Jupiter TestOracle account in Jupiter Perpetuals.
 */
@Data
@Builder
public class JupiterTestOracle {
    private long price;
    private int expo;
    private long conf;
    private long publishTime;

    /**
     * Deserializes a byte array into a JupiterTestOracle object.
     *
     * @param data the byte array representing the account data.
     * @return a JupiterTestOracle object.
     */
    public static JupiterTestOracle fromByteArray(byte[] data) {
        int offset = 8; // Skip discriminator

        long price = JupiterUtil.readInt64(data, offset);
        offset += 8;

        int expo = OpenBookUtil.readInt32(data, offset);
        offset += 4;

        long conf = JupiterUtil.readUint64(data, offset);
        offset += 8;

        long publishTime = JupiterUtil.readInt64(data, offset);

        return JupiterTestOracle.builder()
                .price(price)
                .expo(expo)
                .conf(conf)
                .publishTime(publishTime)
                .build();
    }
}