package com.mmorrell.pyth.model;

import com.mmorrell.pyth.utils.PythUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bitcoinj.core.Utils;

@Builder
@Getter
@Setter
@ToString
public class PriceEma {

    public static final int SIZE = 24;

    private static final int VALUE_COMPONENT_OFFSET = 0;
    private static final int NUMERATOR_OFFSET = VALUE_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int DENOMINATOR_OFFSET = NUMERATOR_OFFSET + PythUtils.INT64_SIZE;

    private long valueComponent;
    private float value;
    private long numerator;
    private long denominator;

    public static PriceEma readPriceEma(byte[] data, int exponent) {
        final PriceEma priceEma = PriceEma.builder()
                .valueComponent(Utils.readInt64(data, VALUE_COMPONENT_OFFSET))
                .numerator(Utils.readInt64(data, NUMERATOR_OFFSET))
                .denominator(Utils.readInt64(data, DENOMINATOR_OFFSET))
                .build();

        priceEma.setValue((float) priceEma.getValueComponent() * (float) Math.pow(10, exponent));
        return priceEma;
    }
}
