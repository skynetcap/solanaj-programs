package com.mmorrell.mango.model;

public class MangoUtils {

    public static final int ACCOUNT_FLAGS_SIZE_BYTES = 8;
    public static final int U64_SIZE_BYTES = 8;

    public static double getBaseSplTokenMultiplier(byte baseDecimals) {
        return Math.pow(10, baseDecimals);
    }

    public static double getQuoteSplTokenMultiplier(byte quoteDecimals) {
        return Math.pow(10, quoteDecimals);
    }

    public static float priceLotsToNumber(long price, byte baseDecimals, byte quoteDecimals, long baseLotSize, long quoteLotSize) {
        double top = (price * quoteLotSize * getBaseSplTokenMultiplier(baseDecimals));
        double bottom = (baseLotSize * getQuoteSplTokenMultiplier(quoteDecimals));

        return (float) (top / bottom);
    }

    public static long priceNumberToLots(float price, byte quoteDecimals, long baseLotSize, byte baseDecimals, long quoteLotSize) {
        double top = (price * Math.pow(10, quoteDecimals) * baseLotSize);
        double bottom = Math.pow(10, baseDecimals) * quoteLotSize;
        return (long) Math.ceil(top / bottom);
    }

    public static float baseSizeLotsToNumber(long size, long baseLotSize, long baseMultiplier) {
        double top = size * baseLotSize;
        return (float) (top / baseMultiplier);
    }

    public static long baseSizeNumberToLots(float size, byte baseDecimals, long baseLotSize) {
        double top = Math.round(size * Math.pow(10, baseDecimals));
        return (long) Math.ceil(top / baseLotSize);
    }

}
