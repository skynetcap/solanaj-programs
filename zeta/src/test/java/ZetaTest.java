import com.mmorrell.common.model.GenericOrder;
import com.mmorrell.common.model.GenericOrderBook;
import com.mmorrell.common.model.Market;
import com.mmorrell.common.model.MarketBuilder;
import com.mmorrell.zeta.model.ZetaOrderBook;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.util.Base64;
import java.util.List;

public class ZetaTest {

    private final RpcClient client = new RpcClient(Cluster.MAINNET);
    private static final Logger LOGGER = LogManager.getLogger(ZetaTest.class);

    @Test
    public void zetaTest() throws RpcException {
        // Bids for market oTVAoRCiHnfEds5MTPerZk6VunEp24bCae8oSVrQmSU
        PublicKey publicKey = new PublicKey("oTVAoRCiHnfEds5MTPerZk6VunEp24bCae8oSVrQmSU");
        System.out.println(publicKey.toBase58());

        byte[] data = Base64.getDecoder().decode(
                client.getApi().getAccountInfo(publicKey).getValue().getData().get(0)
        );

        Market zetaMarket = new MarketBuilder()
                .setClient(client)
                .setPublicKey(publicKey)
                .setDecimals(0, 6)
                .setRetrieveOrderBooks(true)
                .build();

        GenericOrderBook zetaOrderBook = zetaMarket.getBidOrderBook();

        zetaOrderBook.getOrders().forEach(genericOrder -> {
            System.out.println(genericOrder.toString());
        });

        GenericOrderBook zetaAskOrderBook = zetaMarket.getAskOrderBook();

        zetaAskOrderBook.getOrders().forEach(genericOrder -> {
            System.out.println(genericOrder.toString());
        });
    }
}
