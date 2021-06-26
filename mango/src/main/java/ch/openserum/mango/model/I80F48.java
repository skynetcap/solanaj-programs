package ch.openserum.mango.model;

import lombok.*;
import org.p2p.solanaj.utils.ByteUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class I80F48 {

    public static final int I80F48_LENGTH = 16;
    private static final int FRACTIONS = 48;
    private byte[] data;

    public static I80F48 readI80F48(byte[] data, int offset) {
        return new I80F48(Arrays.copyOfRange(data, offset, offset + I80F48_LENGTH));
    }

    public float decodeFloat() {
        return decodeBigDecimal().floatValue();
    }

    public BigDecimal decodeBigDecimal() {
        ByteBuffer buffer = ByteBuffer.allocate(I80F48_LENGTH);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < I80F48_LENGTH; i++) {
            buffer.put(data[I80F48_LENGTH - i - 1]);
        }

        String newString = ByteUtils.bytesToHex(buffer.array());
        BigInteger result = new BigInteger(newString, I80F48_LENGTH);

        return new BigDecimal(result, FRACTIONS)
                .divide(BigDecimal.valueOf((long) Math.pow(2, FRACTIONS), FRACTIONS), RoundingMode.HALF_UP);
    }
}
