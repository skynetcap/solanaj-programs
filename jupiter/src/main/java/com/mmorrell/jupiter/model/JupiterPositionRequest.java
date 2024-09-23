package com.mmorrell.jupiter.model;

import com.mmorrell.jupiter.util.JupiterUtil;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.core.PublicKey;

import java.util.Arrays;

/**
 * Represents a Jupiter PositionRequest account in Jupiter Perpetuals.
 */
@Data
@Builder
@Slf4j
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

    /**
     * Enum representing the type of request change.
     */
    public enum RequestChange {
        None,
        Increase,
        Decrease
    }

    /**
     * Enum representing the type of request.
     */
    public enum RequestType {
        Market,
        Trigger
    }

    /**
     * Enum representing the side of the request.
     */
    public enum Side {
        None,
        Long,
        Short
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

        // Read priceSlippage (Optional u64)
        boolean hasPriceSlippage = data[offset] != 0;
        Long priceSlippage = null;
        if (hasPriceSlippage) {
            priceSlippage = JupiterUtil.readUint64(data, offset + 1);
            offset += 9; // 1 byte for option + 8 bytes for uint64
        } else {
            offset += 1; // Only 1 byte for option
        }

        // Read jupiterMinimumOut (Optional u64)
        boolean hasJupiterMinimumOut = data[offset] != 0;
        Long jupiterMinimumOut = null;
        if (hasJupiterMinimumOut) {
            jupiterMinimumOut = JupiterUtil.readUint64(data, offset + 1);
            offset += 9;
        } else {
            offset += 1;
        }

        // Read preSwapAmount (Optional u64)
        boolean hasPreSwapAmount = data[offset] != 0;
        Long preSwapAmount = null;
        if (hasPreSwapAmount) {
            preSwapAmount = JupiterUtil.readUint64(data, offset + 1);
            offset += 9;
        } else {
            offset += 1;
        }

        // Read triggerPrice (Optional u64)
        boolean hasTriggerPrice = data[offset] != 0;
        Long triggerPrice = null;
        if (hasTriggerPrice) {
            triggerPrice = JupiterUtil.readUint64(data, offset + 1);
            offset += 9;
        } else {
            offset += 1;
        }

        // Read triggerAboveThreshold (Optional boolean)
        boolean hasTriggerAboveThreshold = data[offset] != 0;
        Boolean triggerAboveThreshold = null;
        if (hasTriggerAboveThreshold) {
            triggerAboveThreshold = data[offset + 1] != 0;
            offset += 2; // 1 byte for option + 1 byte for boolean
        } else {
            offset += 1;
        }

        // Read entirePosition (Optional boolean)
        boolean hasEntirePosition = data[offset] != 0;
        Boolean entirePosition = null;
        if (hasEntirePosition) {
            entirePosition = data[offset + 1] != 0;
            offset += 2;
        } else {
            offset += 1;
        }

        boolean executed = data[offset++] != 0;

        long counter = JupiterUtil.readUint64(data, offset);
        offset += 8;

        byte bump = data[offset++];

        // Read referral (Optional PublicKey)
        boolean hasReferral = data[offset] != 0;
        PublicKey referral = null;
        if (hasReferral) {
            referral = PublicKey.readPubkey(data, offset + 1);
            offset += 33; // 1 byte for option + 32 bytes for PublicKey
        } else {
            offset += 1;
        }

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
}