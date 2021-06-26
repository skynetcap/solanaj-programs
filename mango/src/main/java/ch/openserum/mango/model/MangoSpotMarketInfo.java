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

    private PublicKey spotMarket;
    private I80F48 maintAssetWeight;

    public static MangoSpotMarketInfo readMangoSpotMarketInfo(byte[] data) {
        final MangoSpotMarketInfo mangoSpotMarketInfo = MangoSpotMarketInfo.builder()
                .build();

        mangoSpotMarketInfo.setSpotMarket(PublicKey.readPubkey(data, SPOT_MARKET_OFFSET));
        mangoSpotMarketInfo.setMaintAssetWeight(I80F48.readI80F48(data, MAINT_ASSET_WEIGHT_OFFSET));

        return mangoSpotMarketInfo;
    }

}
