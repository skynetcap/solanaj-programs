package ch.openserum.pyth.model;

import ch.openserum.pyth.utils.PythUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private static final int LAST_SLOT_OFFSET = NUM_COMPONENT_PRICES_OFFSET + (2 * PythUtils.INT32_SIZE);
    private static final int VALID_SLOT_OFFSET = LAST_SLOT_OFFSET + PythUtils.INT64_SIZE;
    private static final int TWAP_OFFSET = VALID_SLOT_OFFSET + PythUtils.INT64_SIZE;
    private static final int TWAC_OFFSET = TWAP_OFFSET + PriceEma.SIZE;
    private static final int DRV_1_COMPONENT_OFFSET = TWAC_OFFSET + PriceEma.SIZE;
    private static final int DRV_2_COMPONENT_OFFSET = DRV_1_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int PRODUCT_ACCOUNT_KEY_OFFSET = DRV_2_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int NEXT_PRICE_ACCOUNT_KEY_OFFSET = PRODUCT_ACCOUNT_KEY_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int PREVIOUS_SLOT_OFFSET = NEXT_PRICE_ACCOUNT_KEY_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int PREVIOUS_PRICE_COMPONENT_OFFSET = PREVIOUS_SLOT_OFFSET + PythUtils.INT64_SIZE;
    private static final int PREVIOUS_CONFIDENCE_COMPONENT = PREVIOUS_PRICE_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int DRV_3_COMPONENT_OFFSET = PREVIOUS_CONFIDENCE_COMPONENT + PythUtils.INT64_SIZE;
    private static final int AGGREGATE_PRICE_INFO_OFFSET = DRV_3_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int PRICE_COMPONENTS_OFFSET = AGGREGATE_PRICE_INFO_OFFSET + PythUtils.PRICE_INFO_SIZE;

    // Variables
    private int magicNumber;
    private int version;
    private int type;
    private int size;
    private int priceType;
    private int exponent;
    private int numComponentPrices;
    private long lastSlot;
    private long validSlot;

    private PriceEma twap;
    private PriceEma twac;

    private long drv1Component;
    private float drv1;
    private long drv2Component;
    private float drv2;

    private PublicKey productAccountKey;
    private PublicKey nextPriceAccountKey;
    private long previousSlot;
    private long previousPriceComponent;
    private float previousPrice;
    private long previousConfidenceComponent;
    private float previousConfidence;

    private long drv3Component;
    private float drv3;

    private PriceInfo aggregatePriceInfo;
    private List<PriceComponent> priceComponents; // Up to 32 elements

    public static PriceDataAccount readPriceDataAccount(byte[] data) {
        final PriceDataAccount priceDataAccount = PriceDataAccount.builder()
                .magicNumber(PythUtils.readInt32(data, MAGIC_NUMBER_OFFSET))
                .version(PythUtils.readInt32(data, VERSION_OFFSET))
                .type(PythUtils.readInt32(data, TYPE_OFFSET))
                .size(PythUtils.readInt32(data, SIZE_OFFSET))
                .priceType(PythUtils.readInt32(data, PRICE_TYPE_OFFSET))
                .exponent(PythUtils.readInt32(data, EXPONENT_OFFSET))
                .numComponentPrices(PythUtils.readInt32(data, NUM_COMPONENT_PRICES_OFFSET))
                .lastSlot(Utils.readInt64(data, LAST_SLOT_OFFSET))
                .validSlot(Utils.readInt64(data, VALID_SLOT_OFFSET))
                .build();

        priceDataAccount.setTwap(
                PriceEma.readPriceEma(
                        Arrays.copyOfRange(data, TWAP_OFFSET, TWAP_OFFSET + PriceEma.SIZE),
                        priceDataAccount.getExponent()
                )
        );

        priceDataAccount.setTwac(
                PriceEma.readPriceEma(
                        Arrays.copyOfRange(data, TWAC_OFFSET, TWAC_OFFSET + PriceEma.SIZE),
                        priceDataAccount.getExponent()
                )
        );

        long drv1Component = Utils.readInt64(data, DRV_1_COMPONENT_OFFSET);
        float drv1 = (float) drv1Component * (float) Math.pow(10, priceDataAccount.getExponent());
        priceDataAccount.setDrv1Component(drv1Component);
        priceDataAccount.setDrv1(drv1);

        long drv2Component = Utils.readInt64(data, DRV_2_COMPONENT_OFFSET);
        float drv2 = (float) drv2Component * (float) Math.pow(10, priceDataAccount.getExponent());
        priceDataAccount.setDrv2Component(drv2Component);
        priceDataAccount.setDrv2(drv2);

        long drv3Component = Utils.readInt64(data, DRV_3_COMPONENT_OFFSET);
        float drv3 = (float) drv3Component * (float) Math.pow(10, priceDataAccount.getExponent());
        priceDataAccount.setDrv3Component(drv3Component);
        priceDataAccount.setDrv3(drv3);

        priceDataAccount.setProductAccountKey(PublicKey.readPubkey(data, PRODUCT_ACCOUNT_KEY_OFFSET));
        final PublicKey nextPriceAccountKey = PublicKey.readPubkey(data, NEXT_PRICE_ACCOUNT_KEY_OFFSET);
        if (!nextPriceAccountKey.toBase58().equalsIgnoreCase(PythUtils.EMPTY_PUBKEY)){
            priceDataAccount.setNextPriceAccountKey(nextPriceAccountKey);
        }

        long previousSlot = Utils.readInt64(data, PREVIOUS_SLOT_OFFSET);
        priceDataAccount.setPreviousSlot(previousSlot);

        long previousPriceComponent = Utils.readInt64(data, PREVIOUS_PRICE_COMPONENT_OFFSET);
        float previousPrice = (float) previousPriceComponent * (float) Math.pow(10, priceDataAccount.getExponent());
        priceDataAccount.setPreviousPriceComponent(previousPriceComponent);
        priceDataAccount.setPreviousPrice(previousPrice);

        long previousConfidenceComponent = Utils.readInt64(data, PREVIOUS_CONFIDENCE_COMPONENT);
        float previousConfidence = (float) previousConfidenceComponent * (float) Math.pow(10, priceDataAccount.getExponent());
        priceDataAccount.setPreviousConfidenceComponent(previousConfidenceComponent);
        priceDataAccount.setPreviousConfidence(previousConfidence);

        final PriceInfo aggregatePriceInfo = PriceInfo.readPriceInfo(
                Arrays.copyOfRange(
                        data,
                        AGGREGATE_PRICE_INFO_OFFSET,
                        AGGREGATE_PRICE_INFO_OFFSET + PythUtils.PRICE_INFO_SIZE
                ),
                priceDataAccount.getExponent()
        );
        priceDataAccount.setAggregatePriceInfo(aggregatePriceInfo);

        priceDataAccount.setPriceComponents(new ArrayList<>());
        int offset = PRICE_COMPONENTS_OFFSET;
        boolean shouldContinue = true;
        while (offset < data.length && shouldContinue) {
            final PublicKey publisher = PublicKey.readPubkey(data, offset);
            offset += 32;

            if (!publisher.toBase58().equalsIgnoreCase(PythUtils.EMPTY_PUBKEY)) {
                final PriceInfo aggregate = PriceInfo.readPriceInfo(
                        Arrays.copyOfRange(
                                data,
                                offset,
                                offset + PublicKey.PUBLIC_KEY_LENGTH
                        ),
                        priceDataAccount.getExponent()
                );
                offset += 32;

                final PriceInfo latest = PriceInfo.readPriceInfo(
                        Arrays.copyOfRange(
                                data,
                                offset,
                                offset + PublicKey.PUBLIC_KEY_LENGTH
                        ),
                        priceDataAccount.getExponent()
                );
                offset += 32;
                priceDataAccount.getPriceComponents().add(
                        PriceComponent.builder()
                                .publisher(publisher)
                                .aggregate(aggregate)
                                .latest(latest)
                                .build()
                );
            } else {
                shouldContinue = false;
            }

        }

        return priceDataAccount;
    }
}
