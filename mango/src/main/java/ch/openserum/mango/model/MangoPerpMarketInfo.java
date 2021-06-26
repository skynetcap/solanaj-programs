package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;

@Builder
@Getter
@Setter
@ToString
public class MangoPerpMarketInfo {

    public static final int MANGO_PERP_MARKET_INFO_LAYOUT_SIZE = PublicKey.PUBLIC_KEY_LENGTH
            + (5 * I80F48.I80F48_LENGTH)
            + (2 * MangoUtils.U64_SIZE_BYTES);

    private static final int SPOT_MARKET_OFFSET = 0;
    private static final int MAINT_ASSET_WEIGHT_OFFSET = SPOT_MARKET_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int INIT_ASSET_WEIGHT_OFFSET = MAINT_ASSET_WEIGHT_OFFSET + I80F48.I80F48_LENGTH;
    private static final int MAINT_LIAB_WEIGHT_OFFSET = INIT_ASSET_WEIGHT_OFFSET + I80F48.I80F48_LENGTH;
    private static final int INIT_LIAB_WEIGHT_OFFSET = MAINT_LIAB_WEIGHT_OFFSET + I80F48.I80F48_LENGTH;
    private static final int LIQUIDATION_FEE_OFFSET = INIT_LIAB_WEIGHT_OFFSET + I80F48.I80F48_LENGTH;
    private static final int BASE_LOT_SIZE_OFFSET = LIQUIDATION_FEE_OFFSET + I80F48.I80F48_LENGTH;
    private static final int QUOTE_LOT_SIZE_OFFSET = BASE_LOT_SIZE_OFFSET + MangoUtils.U64_SIZE_BYTES;

    private PublicKey perpMarket;
    private I80F48 maintAssetWeight;
    private I80F48 initAssetWeight;
    private I80F48 maintLiabWeight;
    private I80F48 initLiabWeight;
    private I80F48 liquidationFee;
    private long baseLotSize;
    private long quoteLotSize;


    public static MangoPerpMarketInfo readMangoPerpMarketInfo(byte[] data) {
        final MangoPerpMarketInfo mangoPerpMarketInfo = MangoPerpMarketInfo.builder()
                .build();

        mangoPerpMarketInfo.setPerpMarket(PublicKey.readPubkey(data, SPOT_MARKET_OFFSET));
        mangoPerpMarketInfo.setMaintAssetWeight(I80F48.readI80F48(data, MAINT_ASSET_WEIGHT_OFFSET));
        mangoPerpMarketInfo.setInitAssetWeight(I80F48.readI80F48(data, INIT_ASSET_WEIGHT_OFFSET));
        mangoPerpMarketInfo.setMaintLiabWeight(I80F48.readI80F48(data, MAINT_LIAB_WEIGHT_OFFSET));
        mangoPerpMarketInfo.setInitLiabWeight(I80F48.readI80F48(data, INIT_LIAB_WEIGHT_OFFSET));
        mangoPerpMarketInfo.setLiquidationFee(I80F48.readI80F48(data, LIQUIDATION_FEE_OFFSET));
        mangoPerpMarketInfo.setBaseLotSize(Utils.readInt64(data, BASE_LOT_SIZE_OFFSET));
        mangoPerpMarketInfo.setQuoteLotSize(Utils.readInt64(data, QUOTE_LOT_SIZE_OFFSET));

        return mangoPerpMarketInfo;
    }

}
