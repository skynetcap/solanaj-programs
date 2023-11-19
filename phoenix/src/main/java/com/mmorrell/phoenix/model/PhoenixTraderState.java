package com.mmorrell.phoenix.model;

import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;

/**
 * export const traderStateBeet = new beet.BeetArgsStruct<TraderState>(
 *   [
 *     ["quoteLotsLocked", beet.u64],
 *     ["quoteLotsFree", beet.u64],
 *     ["baseLotsLocked", beet.u64],
 *     ["baseLotsFree", beet.u64],
 *     ["padding", beet.uniformFixedSizeArray(beet.u64, 8)],
 *   ],
 *   "TraderState"
 * );
 */
@Data
@Builder
public class PhoenixTraderState {

    public static final int PHOENIX_TRADER_STATE_SIZE = 32 + 64;

    private long quoteLotsLocked;
    private long quoteLotsFree;
    private long baseLotsLocked;
    private long baseLotsFree;

    public static PhoenixTraderState readPhoenixTraderState(byte[] data) {
        return PhoenixTraderState.builder()
                .quoteLotsLocked(Utils.readInt64(data, 0))
                .quoteLotsFree(Utils.readInt64(data, 8))
                .baseLotsLocked(Utils.readInt64(data, 16))
                .baseLotsFree(Utils.readInt64(data, 24))
                .build();
    }

    // + 64 bytes of padding

}
