package ch.openserum.pyth.listener;

import ch.openserum.pyth.model.PriceDataAccount;
import lombok.AllArgsConstructor;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.ws.listeners.NotificationEventListener;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@AllArgsConstructor
public class PriceDataAccountListener implements NotificationEventListener {

    private final PublicKey publicKey;
    private static final Logger LOGGER = Logger.getLogger(PriceDataAccountListener.class.getName());

    @Override
    public void onNotificationEvent(Object data) {
        if (data != null) {
            final Map<String, Object> objectMap = (Map<String, Object>) data;
            final String base64 = (String)((List) objectMap.get("data")).get(0);

            final PriceDataAccount streamedPriceDataAccount = PriceDataAccount.readPriceDataAccount(
                    Base64.getDecoder().decode(base64)
            );

            LOGGER.info(
                    String.format(
                            "Asset %s, Price = %.6f, Confidence = %.5f",
                            publicKey.toBase58(),
                            streamedPriceDataAccount.getAggregatePriceInfo().getPrice(),
                            streamedPriceDataAccount.getAggregatePriceInfo().getConfidence()

                    )
            );
        }
    }
}
