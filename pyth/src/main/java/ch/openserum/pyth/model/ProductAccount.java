package ch.openserum.pyth.model;

import ch.openserum.pyth.utils.PythUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Builder
@Getter
@Setter
@ToString
public class ProductAccount {

    // Constants
    private static final int MAGIC_NUMBER_OFFSET = 0;
    private static final int VERSION_OFFSET = MAGIC_NUMBER_OFFSET + PythUtils.INT32_SIZE;
    private static final int TYPE_OFFSET = VERSION_OFFSET + PythUtils.INT32_SIZE;
    private static final int SIZE_OFFSET = TYPE_OFFSET + PythUtils.INT32_SIZE;
    private static final int PRICE_ACCOUNT_KEY_OFFSET = SIZE_OFFSET + PythUtils.INT32_SIZE;
    private static final int PRODUCT_ATTRIBUTES_OFFSET = PRICE_ACCOUNT_KEY_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;

    // Variables
    private int magicNumber;
    private int version;
    private int type;
    private int size;
    private PublicKey priceAccountKey;
    private Map<String, String> productAttributes;

    public static ProductAccount readProductAccount(byte[] data) {
        final ProductAccount productAccount = ProductAccount.builder()
                .magicNumber(PythUtils.readInt32(data, MAGIC_NUMBER_OFFSET))
                .version(PythUtils.readInt32(data, VERSION_OFFSET))
                .type(PythUtils.readInt32(data, TYPE_OFFSET))
                .size(PythUtils.readInt32(data, SIZE_OFFSET))
                .priceAccountKey(PublicKey.readPubkey(data, PRICE_ACCOUNT_KEY_OFFSET))
                .build();

        productAccount.setProductAttributes(new HashMap<>());
        int index = PRODUCT_ATTRIBUTES_OFFSET;
        while (index < productAccount.getSize()) {
            int keyLength = data[index];
            index++;
            if (keyLength > 0) {
                String key = new String(
                        Arrays.copyOfRange(data, index, index + keyLength),
                        StandardCharsets.UTF_8
                );
                index += keyLength;

                int valueLength = data[index];
                index++;

                String value = new String(
                        Arrays.copyOfRange(data, index, index + valueLength),
                        StandardCharsets.UTF_8
                );
                index+= valueLength;

                productAccount.getProductAttributes().put(key, value);
            }
        }

        return productAccount;
    }
}
