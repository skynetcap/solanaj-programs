package com.mmorrell.zeta.model;

import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;

@Data
@Builder
public class ZetaProduct {

    private PublicKey marketId;
    private boolean set;
    private long strikeValue;
    private boolean dirty;
    private ZetaProductKind productKind;
    private int expiryIndex;
    private double bestBid, bestAsk;
    private double bestBidQuantity, bestAskQuantity;

    public static ZetaProduct readZetaProduct(byte[] data) {
        ZetaProductKind zetaProductKind = switch(data[42]) {
            case 1 -> ZetaProductKind.CALL;
            case 2 -> ZetaProductKind.PUT;
            case 3 -> ZetaProductKind.FUTURE;
            case 4 -> ZetaProductKind.PERPETUAL;
            default -> ZetaProductKind.UNINITIALIZED;
        };

        return ZetaProduct.builder()
                .marketId(PublicKey.readPubkey(data, 0))
                .set(data[32] == 1)
                .strikeValue(Utils.readInt64(data, 33))
                .dirty(data[41] == 1)
                .productKind(zetaProductKind)
                .build();
    }

    public double getStrikeValueDouble() {
        double strikeDouble = (double) strikeValue;
        double precisionBn = Math.pow(10, 6);
        return ((strikeDouble / precisionBn) + ((strikeDouble % precisionBn) / precisionBn));
    }
}
