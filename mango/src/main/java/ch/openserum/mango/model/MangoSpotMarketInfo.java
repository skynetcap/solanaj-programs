package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;

@Builder
@Getter
@Setter
@ToString
public class MangoSpotMarketInfo {

    public static final int MANGO_SPOT_MARKET_INFO_LAYOUT_SIZE = PublicKey.PUBLIC_KEY_LENGTH
            + (5 * I80F48.I80F48_LENGTH);
    private static final int SPOT_MARKET_OFFSET = 0;
    private static final int MAINT_ASSET_WEIGHT_OFFSET = SPOT_MARKET_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int INIT_ASSET_WEIGHT_OFFSET = MAINT_ASSET_WEIGHT_OFFSET + I80F48.I80F48_LENGTH;
    private static final int MAINT_LIAB_WEIGHT_OFFSET = INIT_ASSET_WEIGHT_OFFSET + I80F48.I80F48_LENGTH;
    private static final int INIT_LIAB_WEIGHT_OFFSET = MAINT_LIAB_WEIGHT_OFFSET + I80F48.I80F48_LENGTH;
    private static final int LIQUIDATION_FEE_OFFSET = INIT_LIAB_WEIGHT_OFFSET + I80F48.I80F48_LENGTH;

    private PublicKey spotMarket;
    private I80F48 maintAssetWeight;
    private I80F48 initAssetWeight;
    private I80F48 maintLiabWeight;
    private I80F48 initLiabWeight;
    private I80F48 liquidationFee;


    public static MangoSpotMarketInfo readMangoSpotMarketInfo(byte[] data) {
        final MangoSpotMarketInfo mangoSpotMarketInfo = MangoSpotMarketInfo.builder()
                .build();

        mangoSpotMarketInfo.setSpotMarket(PublicKey.readPubkey(data, SPOT_MARKET_OFFSET));
        mangoSpotMarketInfo.setMaintAssetWeight(I80F48.readI80F48(data, MAINT_ASSET_WEIGHT_OFFSET));
        mangoSpotMarketInfo.setInitAssetWeight(I80F48.readI80F48(data, INIT_ASSET_WEIGHT_OFFSET));
        mangoSpotMarketInfo.setMaintLiabWeight(I80F48.readI80F48(data, MAINT_LIAB_WEIGHT_OFFSET));
        mangoSpotMarketInfo.setInitLiabWeight(I80F48.readI80F48(data, INIT_LIAB_WEIGHT_OFFSET));
        mangoSpotMarketInfo.setLiquidationFee(I80F48.readI80F48(data, LIQUIDATION_FEE_OFFSET));


        return mangoSpotMarketInfo;
    }

}
