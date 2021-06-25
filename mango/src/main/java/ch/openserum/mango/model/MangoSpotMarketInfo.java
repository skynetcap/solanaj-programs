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

    private PublicKey spotMarket;
    private I80F48 maintAssetWeight;

    public static MangoSpotMarketInfo readMangoSpotMarketInfo(byte[] data) {
        final MangoSpotMarketInfo mangoSpotMarketInfo = MangoSpotMarketInfo.builder()
                .build();

        mangoSpotMarketInfo.setSpotMarket(PublicKey.readPubkey(data, SPOT_MARKET_OFFSET));

        return mangoSpotMarketInfo;
    }

}
