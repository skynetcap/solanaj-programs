package com.mmorrell.openbook;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class OpenBookUtil {

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

}
