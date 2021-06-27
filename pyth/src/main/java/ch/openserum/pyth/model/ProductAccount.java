package ch.openserum.pyth.model;

import ch.openserum.pyth.utils.PythUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class ProductAccount {

    // Constants
    private static final int MAGIC_NUMBER_OFFSET = 0;

    // Variables
    private int magicNumber;

    public static ProductAccount readProductAccount(byte[] data) {
        final ProductAccount productAccount = ProductAccount.builder()
                .magicNumber(PythUtils.readInt32(data, MAGIC_NUMBER_OFFSET))
                .build();

        return productAccount;
    }
}
