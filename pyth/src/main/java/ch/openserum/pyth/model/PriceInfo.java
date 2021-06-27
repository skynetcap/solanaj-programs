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
public class PriceInfo {

    private static final int PRICE_COMPONENT_OFFSET = 0;
    private static final int CONFIDENCE_COMPONENT_OFFSET = PRICE_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int STATUS_OFFSET = CONFIDENCE_COMPONENT_OFFSET + PythUtils.INT64_SIZE;
    private static final int CORPORATE_ACTION_OFFSET = STATUS_OFFSET + PythUtils.INT32_SIZE;
    private static final int PUBLISH_SLOT_OFFSET = CORPORATE_ACTION_OFFSET + PythUtils.INT32_SIZE;

    private long priceComponent;
    private float price;
    private long confidenceComponent;
    private float confidence;
    private int status;
    private int corporateAction;
    private long publishSlot;

    public static PriceInfo readPriceInfo(byte[] data, int exponent) {
        final PriceInfo priceInfo = PriceInfo.builder()
                .priceComponent(Utils.readInt64(data, PRICE_COMPONENT_OFFSET))
                .confidenceComponent(Utils.readInt64(data, CONFIDENCE_COMPONENT_OFFSET))
                .status(PythUtils.readInt32(data, STATUS_OFFSET))
                .corporateAction(PythUtils.readInt32(data, CORPORATE_ACTION_OFFSET))
                .publishSlot(Utils.readInt64(data, PUBLISH_SLOT_OFFSET))
                .build();

        // Calculate necessary values for setters
        priceInfo.setPrice(((float) priceInfo.getPriceComponent()) * (float) Math.pow(10, exponent));
        priceInfo.setConfidence(((float) priceInfo.getConfidenceComponent()) * (float) Math.pow(10, exponent));

        return priceInfo;
    }

}
