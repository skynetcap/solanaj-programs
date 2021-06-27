package ch.openserum.pyth.model;

import ch.openserum.pyth.utils.PythUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;

import java.util.Arrays;

@Builder
@Getter
@Setter
@ToString
public class PriceDataAccount {

    private static final int NUM_DERIVED_VALUES = 6;

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
    private static final int PRODUCT_ACCOUNT_KEY_OFFSET = AVOL_COMPONENT_OFFSET + PythUtils.INT64_SIZE
            + (NUM_DERIVED_VALUES * PythUtils.INT64_SIZE);
    private static final int NEXT_PRICE_ACCOUNT_KEY_OFFSET = PRODUCT_ACCOUNT_KEY_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int AGGREGATE_PRICE_UPDATE_ACCOUNT_KEY_OFFSET = NEXT_PRICE_ACCOUNT_KEY_OFFSET
            + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int AGGREGATE_PRICE_INFO_OFFSET = AGGREGATE_PRICE_UPDATE_ACCOUNT_KEY_OFFSET
            + PublicKey.PUBLIC_KEY_LENGTH;

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
    private PublicKey productAccountKey;
    private PublicKey nextPriceAccountKey;
    private PublicKey aggregatePriceUpdaterAccountKey;
    private PriceInfo aggregatePriceInfo;

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

        priceDataAccount.setProductAccountKey(PublicKey.readPubkey(data, PRODUCT_ACCOUNT_KEY_OFFSET));
        final PublicKey nextPriceAccountKey = PublicKey.readPubkey(data, NEXT_PRICE_ACCOUNT_KEY_OFFSET);
        if (!nextPriceAccountKey.toBase58().equalsIgnoreCase(PythUtils.EMPTY_PUBKEY)){
            priceDataAccount.setNextPriceAccountKey(nextPriceAccountKey);
        }

        priceDataAccount.setAggregatePriceUpdaterAccountKey(
                PublicKey.readPubkey(data, AGGREGATE_PRICE_UPDATE_ACCOUNT_KEY_OFFSET)
        );

        final PriceInfo aggregatePriceInfo = PriceInfo.readPriceInfo(
                Arrays.copyOfRange(
                        data,
                        AGGREGATE_PRICE_INFO_OFFSET,
                        AGGREGATE_PRICE_INFO_OFFSET + PythUtils.PRICE_INFO_SIZE
                ),
                priceDataAccount.getExponent()
        );
        priceDataAccount.setAggregatePriceInfo(aggregatePriceInfo);

        return priceDataAccount;
    }
}
