package com.mmorrell.pyth.listener;

import com.mmorrell.pyth.model.PriceDataAccount;
import lombok.AllArgsConstructor;
import org.p2p.solanaj.ws.listeners.NotificationEventListener;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@AllArgsConstructor
public class PriceDataAccountListener implements NotificationEventListener {

    private static final Logger LOGGER = Logger.getLogger(PriceDataAccountListener.class.getName());
    private final Map<String, Float> currentPriceMap;
    private final String productName;

    @Override
    public void onNotificationEvent(Object data) {
        if (data != null) {
            final Map<String, Object> objectMap = (Map<String, Object>) data;
            final String base64 = (String)((List) objectMap.get("data")).get(0);

            final PriceDataAccount streamedPriceDataAccount = PriceDataAccount.readPriceDataAccount(
                    Base64.getDecoder().decode(base64)
            );

            currentPriceMap.put(productName, streamedPriceDataAccount.getAggregatePriceInfo().getPrice());
            LOGGER.info(currentPriceMap.toString());
        }
    }
}
