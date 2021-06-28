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
    private static final int CURRENT_SLOT_OFFSET = NUM_COMPONENT_PRICES_OFFSET + (2 * PythUtils.INT32_SIZE);
    private static final int VALID_SLOT_OFFSET = CURRENT_SLOT_OFFSET + PythUtils.INT64_SIZE;
    private static final int TWAP_COMPONENT_OFFSET = VALID_SLOT_OFFSET + PythUtils.INT64_SIZE;
    private static final int AVOL_COMPONENT_OFFSET = TWAP_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int DRV_0_COMPONENT_OFFSET = AVOL_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int DRV_1_COMPONENT_OFFSET = DRV_0_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int DRV_2_COMPONENT_OFFSET = DRV_1_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int DRV_3_COMPONENT_OFFSET = DRV_2_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int DRV_4_COMPONENT_OFFSET = DRV_3_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int DRV_5_COMPONENT_OFFSET = DRV_4_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int PRODUCT_ACCOUNT_KEY_OFFSET = DRV_5_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int NEXT_PRICE_ACCOUNT_KEY_OFFSET = PRODUCT_ACCOUNT_KEY_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int AGGREGATE_PRICE_UPDATE_ACCOUNT_KEY_OFFSET = NEXT_PRICE_ACCOUNT_KEY_OFFSET
            + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int AGGREGATE_PRICE_INFO_OFFSET = AGGREGATE_PRICE_UPDATE_ACCOUNT_KEY_OFFSET
            + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int PRICE_COMPONENTS_OFFSET = AGGREGATE_PRICE_INFO_OFFSET + PythUtils.PRICE_INFO_SIZE;

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

    // Space for future derived values
    private long drv0Component;
    private float drv0;
    private long drv1Component;
    private float drv1;
    private long drv2Component;
    private float drv2;
    private long drv3Component;
    private float drv3;
    private long drv4Component;
    private float drv4;
    private long drv5Component;
    private float drv5;

    private PublicKey productAccountKey;
    private PublicKey nextPriceAccountKey;
    private PublicKey aggregatePriceUpdaterAccountKey;
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
                .currentSlot(Utils.readInt64(data, CURRENT_SLOT_OFFSET))
                .validSlot(Utils.readInt64(data, VALID_SLOT_OFFSET))
                .twapComponent(Utils.readInt64(data, TWAP_COMPONENT_OFFSET))
                .build();

        float twap = ((float) priceDataAccount.getTwapComponent()) * (float) (Math.pow(10, priceDataAccount.getExponent()));
        priceDataAccount.setTwap(twap);

        priceDataAccount.setAvolComponent(Utils.readInt64(data, AVOL_COMPONENT_OFFSET));
        float avol = ((float) priceDataAccount.getAvolComponent()) * (float) (Math.pow(10, priceDataAccount.getExponent()));
        priceDataAccount.setAvol(avol);

        // Derived values
        long drv0Component = Utils.readInt64(data, DRV_0_COMPONENT_OFFSET);
        float drv0 = (float) drv0Component * (float) Math.pow(10, priceDataAccount.getExponent());
        priceDataAccount.setDrv0Component(drv0Component);
        priceDataAccount.setDrv0(drv0);

        long drv1Component = Utils.readInt64(data, DRV_1_COMPONENT_OFFSET);
        float drv1 = (float) drv1Component * (float) Math.pow(10, priceDataAccount.getExponent());
        priceDataAccount.setDrv1Component(drv1Component);
        priceDataAccount.setDrv0(drv1);

        long drv2Component = Utils.readInt64(data, DRV_2_COMPONENT_OFFSET);
        float drv2 = (float) drv2Component * (float) Math.pow(10, priceDataAccount.getExponent());
        priceDataAccount.setDrv2Component(drv2Component);
        priceDataAccount.setDrv2(drv2);

        long drv3Component = Utils.readInt64(data, DRV_3_COMPONENT_OFFSET);
        float drv3 = (float) drv3Component * (float) Math.pow(10, priceDataAccount.getExponent());
        priceDataAccount.setDrv3Component(drv3Component);
        priceDataAccount.setDrv3(drv3);

        long drv4Component = Utils.readInt64(data, DRV_4_COMPONENT_OFFSET);
        float drv4 = (float) drv4Component * (float) Math.pow(10, priceDataAccount.getExponent());
        priceDataAccount.setDrv4Component(drv4Component);
        priceDataAccount.setDrv4(drv4);

        long drv5Component = Utils.readInt64(data, DRV_5_COMPONENT_OFFSET);
        float drv5 = (float) drv5Component * (float) Math.pow(10, priceDataAccount.getExponent());
        priceDataAccount.setDrv5Component(drv5Component);
        priceDataAccount.setDrv5(drv5);

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
