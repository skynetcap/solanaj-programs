import com.mmorrell.common.model.GenericOrderBook;
import com.mmorrell.common.model.Market;
import com.mmorrell.common.model.MarketBuilder;
import com.mmorrell.zeta.model.ZetaGroup;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.util.Base64;

public class ZetaTest {

    private final RpcClient client = new RpcClient(Cluster.MAINNET);

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

        // TODO - Add reverse lookup for OOA > Zeta account.
        // This requires going from Zeta > OOA, and matching, since OOA always has the same owner.
        // E.g. we need a function: getOwnerOfZetaOoa(ooaPubkey)
    }

    @Test
    public void zetaGroupTest() throws RpcException {
        PublicKey zetaGroupPubkey = new PublicKey("CoGhjFdyqzMFr5xVgznuBjULvoFbFtNN4bCdQzRArNK2");
        System.out.println(zetaGroupPubkey.toBase58());

        byte[] data = Base64.getDecoder().decode(
                client.getApi().getAccountInfo(zetaGroupPubkey).getValue().getData().get(0)
        );

        ZetaGroup zetaGroup = ZetaGroup.readZetaGroup(data);

        System.out.println(zetaGroup.toString());
    }
}
