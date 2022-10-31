import com.mmorrell.common.model.GenericOrderBook;
import com.mmorrell.common.model.Market;
import com.mmorrell.common.model.MarketBuilder;
import com.mmorrell.zeta.model.ZetaGroup;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Slf4j
public class ZetaTest {

    private final RpcClient client = new RpcClient(Cluster.MAINNET);

    @Test
    public void zetaTest() throws RpcException {
        // Bids for market 21iZtRQWqBCBmq5oSCScpgU9JNEFLyEfWxdvpJxZuA8N
        PublicKey publicKey = new PublicKey("21iZtRQWqBCBmq5oSCScpgU9JNEFLyEfWxdvpJxZuA8N");
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

        // Create account from private key
        Account account;
        try {
            account = Account.fromJson(Files.readString(Paths.get("src/main/resources/serummainnet.json")));
            log.info("Pubkey = " + account.getPublicKey());
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

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

        zetaGroup.getZetaProducts().forEach(zetaProduct -> {
            System.out.println(zetaProduct.toString());
        });
    }
}
