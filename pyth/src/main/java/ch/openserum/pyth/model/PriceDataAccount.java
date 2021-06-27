package ch.openserum.pyth.model;

import ch.openserum.pyth.utils.PythUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bitcoinj.core.Utils;

@Builder
@Getter
@Setter
@ToString
public class PriceDataAccount {

    // Offsets
    private static final int MAGIC_NUMBER_OFFSET = 0;
    private static final int VERSION_OFFSET = MAGIC_NUMBER_OFFSET + PythUtils.INT32_SIZE;
    private static final int TYPE_OFFSET = VERSION_OFFSET + PythUtils.INT32_SIZE;
    private static final int SIZE_OFFSET = TYPE_OFFSET + PythUtils.INT32_SIZE;
    private static final int PRICE_TYPE_OFFSET = SIZE_OFFSET + PythUtils.INT32_SIZE;
    private static final int EXPONENT_OFFSET = PRICE_TYPE_OFFSET + PythUtils.INT32_SIZE;
    private static final int NUM_COMPONENT_PRICES_OFFSET = EXPONENT_OFFSET + PythUtils.INT32_SIZE;
    private static final int CURRENT_SLOT_OFFSET = NUM_COMPONENT_PRICES_OFFSET + (2 * PythUtils.INT32_SIZE);
    private static final int VALID_SLOT_OFFSET = CURRENT_SLOT_OFFSET + PythUtils.INT64_SIZE;
    private static final int TWAP_COMPONENT_OFFSET = VALID_SLOT_OFFSET + PythUtils.INT64_SIZE;
    private static final int AVOL_COMPONENT_OFFSET = TWAP_COMPONENT_OFFSET + PythUtils.INT64_SIZE;

    // Variables
    private int magicNumber;
    private int version;
    private int type;
    private int size;
    private int priceType;
    private int exponent;
    private int numComponentPrices;
    private long currentSlot;
    private long validSlot;
    private long twapComponent;
    private float twap;
    private long avolComponent;
    private float avol;

    public static PriceDataAccount readPriceDataAccount(byte[] data) {
        final PriceDataAccount priceDataAccount = PriceDataAccount.builder()
                .magicNumber(PythUtils.readInt32(data, MAGIC_NUMBER_OFFSET))
                .version(PythUtils.readInt32(data, VERSION_OFFSET))
                .type(PythUtils.readInt32(data, TYPE_OFFSET))
                .size(PythUtils.readInt32(data, SIZE_OFFSET))
                .priceType(PythUtils.readInt32(data, PRICE_TYPE_OFFSET))
                .exponent(PythUtils.readInt32(data, EXPONENT_OFFSET))
                .numComponentPrices(PythUtils.readInt32(data, NUM_COMPONENT_PRICES_OFFSET))
                .currentSlot(Utils.readInt64(data, CURRENT_SLOT_OFFSET))
                .validSlot(Utils.readInt64(data, VALID_SLOT_OFFSET))
                .twapComponent(Utils.readInt64(data, TWAP_COMPONENT_OFFSET))
                .build();

        float twap = ((float) priceDataAccount.getTwapComponent()) * (float) (Math.pow(10, priceDataAccount.getExponent()));
        priceDataAccount.setTwap(twap);

        priceDataAccount.setAvolComponent(Utils.readInt64(data, AVOL_COMPONENT_OFFSET));
        float avol = ((float) priceDataAccount.getAvolComponent()) * (float) (Math.pow(10, priceDataAccount.getExponent()));
        priceDataAccount.setAvol(avol);

        return priceDataAccount;
    }
}
