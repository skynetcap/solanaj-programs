package com.mmorrell.openbook;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.bitcoinj.core.Utils.reverseBytes;
import static org.p2p.solanaj.utils.ByteUtils.readBytes;

public class OpenBookUtil {

    public final static byte[] MARKET_DISCRIMINATOR = {
            (byte) 0xDB, (byte) 0xBE, (byte) 0xD5, (byte) 0x37, (byte) 0x00, (byte) 0xE3,
            (byte) 0xC6, (byte) 0x9A
    };

    /**
     * Encodes the "global::initialize" sighash
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

}
