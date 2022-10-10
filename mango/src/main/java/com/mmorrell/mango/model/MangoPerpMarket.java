package com.mmorrell.mango.model;

import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;

@Data
@Builder
// 4nfmQP3KmUqEJ6qJLsS3offKgE96YUB4Rp7UQvm2Fbi9 mngo-perp
public class MangoPerpMarket {

    // metadata 0-7 ignored
    private static final int MANGO_GROUP_OFFSET = 8;
    private static final int BIDS_OFFSET = MANGO_GROUP_OFFSET + 32;
    private static final int ASKS_OFFSET = BIDS_OFFSET + 32;
    private static final int EVENT_QUEUE_OFFSET = ASKS_OFFSET + 32;
    private static final int QUOTE_LOT_SIZE_OFFSET = EVENT_QUEUE_OFFSET + 32;
    private static final int BASE_LOT_SIZE_OFFSET = QUOTE_LOT_SIZE_OFFSET + 8;

    private PublicKey mangoGroup;
    private PublicKey bids;
    private PublicKey asks;
    private PublicKey eventQueue;
    private long quoteLotSize;
    private long baseLotSize;

    public static MangoPerpMarket readMangoPerpMarket(byte[] data) {
        final MangoPerpMarket mangoPerpMarket = MangoPerpMarket.builder()
                .mangoGroup(PublicKey.readPubkey(data, MANGO_GROUP_OFFSET))
                .bids(PublicKey.readPubkey(data, BIDS_OFFSET))
                .asks(PublicKey.readPubkey(data, ASKS_OFFSET))
                .eventQueue(PublicKey.readPubkey(data, EVENT_QUEUE_OFFSET))
                .quoteLotSize(Utils.readInt64(data, QUOTE_LOT_SIZE_OFFSET))
                .baseLotSize(Utils.readInt64(data, BASE_LOT_SIZE_OFFSET))
                .build();

        return mangoPerpMarket;
    }
}
