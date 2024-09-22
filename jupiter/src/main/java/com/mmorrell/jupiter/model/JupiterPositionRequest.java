package com.mmorrell.jupiter.model;

import com.mmorrell.jupiter.util.JupiterUtil;
import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;

/**
 * Represents a Jupiter PositionRequest account in Jupiter Perpetuals.
 */
@Data
@Builder
public class JupiterPositionRequest {
    private PublicKey owner;
    private PublicKey pool;
    private PublicKey custody;
    private PublicKey position;
    private PublicKey mint;
    private long openTime;
    private long updateTime;
    private long sizeUsdDelta;
    private long collateralDelta;
    private RequestChange requestChange;
    private RequestType requestType;
    private Side side;
    private Long priceSlippage;
    private Long jupiterMinimumOut;
    private Long preSwapAmount;
    private Long triggerPrice;
    private Boolean triggerAboveThreshold;
    private Boolean entirePosition;
    private boolean executed;
    private long counter;
    private byte bump;
    private PublicKey referral;

    public enum RequestChange {
        NO_CHANGE,
        INCREASE,
        DECREASE,
        CLOSE
    }

    public enum RequestType {
        MARKET,
        LIMIT,
        STOP
    }

    public enum Side {
        LONG,
        SHORT
    }

    /**
     * Deserializes a byte array into a JupiterPositionRequest object.
     *
     * @param data the byte array representing the account data.
     * @return a JupiterPositionRequest object.
     */
    public static JupiterPositionRequest fromByteArray(byte[] data) {
        int offset = 8; // Skip discriminator

        PublicKey owner = PublicKey.readPubkey(data, offset);
        offset += 32;

        PublicKey pool = PublicKey.readPubkey(data, offset);
        offset += 32;

        PublicKey custody = PublicKey.readPubkey(data, offset);
        offset += 32;

        PublicKey position = PublicKey.readPubkey(data, offset);
        offset += 32;

        PublicKey mint = PublicKey.readPubkey(data, offset);
        offset += 32;

        long openTime = JupiterUtil.readInt64(data, offset);
        offset += 8;

        long updateTime = JupiterUtil.readInt64(data, offset);
        offset += 8;

        long sizeUsdDelta = JupiterUtil.readUint64(data, offset);
        offset += 8;

        long collateralDelta = JupiterUtil.readUint64(data, offset);
        offset += 8;

        RequestChange requestChange = RequestChange.values()[data[offset++]];
        RequestType requestType = RequestType.values()[data[offset++]];
        Side side = Side.values()[data[offset++]];

        Long priceSlippage = JupiterUtil.readOptionalUint64(data, offset);
        offset += 9;

        Long jupiterMinimumOut = JupiterUtil.readOptionalUint64(data, offset);
        offset += 9;

        Long preSwapAmount = JupiterUtil.readOptionalUint64(data, offset);
        offset += 9;

        Long triggerPrice = JupiterUtil.readOptionalUint64(data, offset);
        offset += 9;

        Boolean triggerAboveThreshold = JupiterUtil.readOptionalBoolean(data, offset);
        offset += 2;

        Boolean entirePosition = JupiterUtil.readOptionalBoolean(data, offset);
        offset += 2;

        boolean executed = data[offset++] != 0;

        long counter = JupiterUtil.readUint64(data, offset);
        offset += 8;

        byte bump = data[offset++];

        PublicKey referral = JupiterUtil.readOptionalPublicKey(data, offset);

        return JupiterPositionRequest.builder()
                .owner(owner)
                .pool(pool)
                .custody(custody)
                .position(position)
                .mint(mint)
                .openTime(openTime)
                .updateTime(updateTime)
                .sizeUsdDelta(sizeUsdDelta)
                .collateralDelta(collateralDelta)
                .requestChange(requestChange)
                .requestType(requestType)
                .side(side)
                .priceSlippage(priceSlippage)
                .jupiterMinimumOut(jupiterMinimumOut)
                .preSwapAmount(preSwapAmount)
                .triggerPrice(triggerPrice)
                .triggerAboveThreshold(triggerAboveThreshold)
                .entirePosition(entirePosition)
                .executed(executed)
                .counter(counter)
                .bump(bump)
                .referral(referral)
                .build();
    }

    private static Long readOptionalUint64(byte[] data, int offset) {
        boolean hasValue = data[offset++] != 0;
        return hasValue ? JupiterUtil.readUint64(data, offset) : null;
    }

    private static Boolean readOptionalBoolean(byte[] data, int offset) {
        boolean hasValue = data[offset++] != 0;
        return hasValue ? data[offset] != 0 : null;
    }

    private static PublicKey readOptionalPublicKey(byte[] data, int offset) {
        boolean hasValue = data[offset++] != 0;
        return hasValue ? PublicKey.readPubkey(data, offset) : null;
    }
}