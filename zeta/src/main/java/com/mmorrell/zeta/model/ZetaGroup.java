package com.mmorrell.zeta.model;

import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
public class ZetaGroup {

    private static final int NUM_PRODUCTS = 46;
    private static final int NUM_PRODUCTS_PADDING = 91;
    private static final int NUM_EXPIRIES = 2;

    private static final int PRODUCT_SIZE_BYTES = 43;
    private static final int EXPIRY_SIZE_BYTES = 32;

    private static final int PRODUCTS_OFFSET = 507;
    private static final int EXPIRIES_OFFSET = 6441;

    private PublicKey publicKey;
    private List<ZetaProduct> zetaProducts;
    private List<ZetaExpiry> zetaExpiries;
    private ZetaProduct zetaPerp;

    public static ZetaGroup readZetaGroup(byte[] data) {
        final List<ZetaProduct> zetaProducts = new ArrayList<>(NUM_PRODUCTS);
        final List<ZetaExpiry> zetaExpiries = new ArrayList<>(NUM_EXPIRIES);

        final ZetaGroup zetaGroup = ZetaGroup.builder()
                .zetaProducts(zetaProducts)
                .zetaExpiries(zetaExpiries)
                .build();

        double productsPerExpiry = 23;

        // Zeta Products
        for (int i = 0; i < NUM_PRODUCTS; i++) {
            int offset = PRODUCTS_OFFSET + (i * PRODUCT_SIZE_BYTES);

            final ZetaProduct zetaProduct = ZetaProduct.readZetaProduct(
                    Arrays.copyOfRange(
                            data,
                            offset,
                            offset + PRODUCT_SIZE_BYTES
                    )
            );

            int productExpiryIndex = (int) Math.floor(i / productsPerExpiry);
            zetaProduct.setExpiryIndex(productExpiryIndex);

            zetaProducts.add(zetaProduct);
        }

        // Zeta perp
        final ZetaProduct zetaPerp = ZetaProduct.readZetaProduct(
                Arrays.copyOfRange(
                        data,
                        PRODUCTS_OFFSET + (NUM_PRODUCTS * PRODUCT_SIZE_BYTES) + (NUM_PRODUCTS_PADDING * PRODUCT_SIZE_BYTES),
                        PRODUCTS_OFFSET + (NUM_PRODUCTS * PRODUCT_SIZE_BYTES) + (NUM_PRODUCTS_PADDING * PRODUCT_SIZE_BYTES) + PRODUCT_SIZE_BYTES
                )
        );

        zetaGroup.setZetaPerp(zetaPerp);
        zetaGroup.getZetaProducts().add(zetaPerp);

        // Zeta Expiries
        for (int i = 0; i < NUM_EXPIRIES; i++) {
            int offset = EXPIRIES_OFFSET + (i * EXPIRY_SIZE_BYTES);

            final ZetaExpiry zetaExpiry = ZetaExpiry.readZetaExpiry(
                    Arrays.copyOfRange(
                            data,
                            offset,
                            offset + EXPIRY_SIZE_BYTES
                    )
            );

            zetaExpiries.add(zetaExpiry);
        }

        return zetaGroup;
    }

}
