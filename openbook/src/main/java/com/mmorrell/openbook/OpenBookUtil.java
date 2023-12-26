package com.mmorrell.openbook;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.bitcoinj.core.Utils.reverseBytes;
import static org.p2p.solanaj.utils.ByteUtils.readBytes;

/**
 * The OpenBookUtil class provides utility methods for working with OpenBook data.
 */
public class OpenBookUtil {

    public final static byte[] MARKET_DISCRIMINATOR = {
            (byte) 0xDB, (byte) 0xBE, (byte) 0xD5, (byte) 0x37, (byte) 0x00, (byte) 0xE3,
            (byte) 0xC6, (byte) 0x9A
    };

    public final static byte[] EVENT_HEAP_DISCRIMINATOR = {
            (byte) 0x77, (byte) 0x3B, (byte) 0x3D, (byte) 0x13, (byte) 0xA5, (byte) 0x54,
            (byte) 0x39, (byte) 0xAF
    };

    public final static byte[] CONSUME_EVENTS_DISCRIMINATOR = {
            (byte) 0xDD, (byte) 0x91, (byte) 0xB1, (byte) 0x34, (byte) 0x1F, (byte) 0x2F,
            (byte) 0x3F, (byte) 0xC9
    };


    /**
     * Encodes the "global::initialize" sighash
     *
     * @return byte array containing sighash for "global::initialize"
     */
    public static byte[] encodeNamespace(String namespace) {
        MessageDigest digest = null;
        byte[] encodedHash = null;
        int sigHashStart = 0;
        int sigHashEnd = 8;

        try {
            digest = MessageDigest.getInstance("SHA-256");
            encodedHash = Arrays.copyOfRange(
                    digest.digest(
                            namespace.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    ),
                    sigHashStart,
                    sigHashEnd
            );
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return encodedHash;
    }

    public static BigInteger readUint128(byte[] buf, int offset) {
        return new BigInteger(reverseBytes(readBytes(buf, offset, 16)));
    }

    public static int readInt32(byte[] data, int offset) {
        // convert 4 bytes into an int.
        // create a byte buffer and wrap the array
        ByteBuffer bb = ByteBuffer.wrap(
                Arrays.copyOfRange(
                        data,
                        offset,
                        offset + 4
                )
        );

        // if the file uses little endian as apposed to network
        // (big endian, Java's native) format,
        // then set the byte order of the ByteBuffer
        bb.order(ByteOrder.LITTLE_ENDIAN);

        // read your integers using ByteBuffer's getInt().
        // four bytes converted into an integer!
        return bb.getInt(0);
    }

    /**
     * Calculates the numerical representation of the price in an open book market.
     *
     * @param price         The price of the order
     * @param baseDecimals  The number of decimal places in the base currency
     * @param quoteDecimals The number of decimal places in the quote currency
     * @param baseLotSize   The size of a base lot
     * @param quoteLotSize  The size of a quote lot
     * @return The numerical representation of the price
     */
    public static double priceLotsToNumber(long price, byte baseDecimals, byte quoteDecimals, long baseLotSize,
                                           long quoteLotSize) {
        double top = (price * quoteLotSize * getBaseSplTokenMultiplier(baseDecimals));
        double bottom = (baseLotSize * getQuoteSplTokenMultiplier(quoteDecimals));

        return (top / bottom);
    }

    public static double getBaseSplTokenMultiplier(byte baseDecimals) {
        return Math.pow(10, baseDecimals);
    }

    public static double getQuoteSplTokenMultiplier(byte quoteDecimals) {
        return Math.pow(10, quoteDecimals);
    }

}
