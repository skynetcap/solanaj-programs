package com.mmorrell.phoenix.model;

import com.mmorrell.phoenix.util.PhoenixUtil;
import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;

@Data
@Builder
public class PhoenixMarketHeader {

    private long discriminant;
    private long status;
    private long bidsSize;
    private long asksSize;
    private long numSeats;

    // Token params

    // Base token
    private int baseDecimals;
    private int baseVaultBump;
    private PublicKey baseMintKey;
    private PublicKey baseVaultKey;
    private long baseLotSize;

    public static PhoenixMarketHeader readPhoenixMarketHeader(byte[] data) {
        return PhoenixMarketHeader.builder()
                .discriminant(Utils.readInt64(data, 0))
                .status(Utils.readInt64(data, 8))
                .bidsSize(Utils.readInt64(data, 16))
                .asksSize(Utils.readInt64(data, 24))
                .numSeats(Utils.readInt64(data, 32))
                .baseDecimals(PhoenixUtil.readInt32(data, 40))
                .baseVaultBump(PhoenixUtil.readInt32(data, 44))
                .baseMintKey(PublicKey.readPubkey(data, 48))
                .baseVaultKey(PublicKey.readPubkey(data, 80))
                .baseLotSize(Utils.readInt64(data, 112))
                .build();
    }

}
