package ch.openserum.pyth.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bitcoinj.core.Utils;

/**
 * Represents a Pyth price info account
 */
@Builder
@Getter
@Setter
@ToString
public class PriceInfoAccount {

    private static final int PRICE_COMPONENT_OFFSET = 0;

    private long priceComponent;
    private float price;

    public static PriceInfoAccount readPriceInfoAccount(byte[] data) {
        final PriceInfoAccount priceInfoAccount = PriceInfoAccount.builder()
                .priceComponent(Utils.readInt64(data, PRICE_COMPONENT_OFFSET))
                .build();

        //priceInfoAccount.setPrice(((float) priceInfoAccount.getPriceComponent()) * Math.pow(10, ));

        return priceInfoAccount;
    }

}
