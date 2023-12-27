import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.mmorrell.openbook.manager.OpenBookManager;
import com.mmorrell.openbook.model.BookSide;
import com.mmorrell.openbook.model.LeafNode;
import com.mmorrell.openbook.model.NodeTag;
import com.mmorrell.openbook.model.OpenBookEventHeap;
import com.mmorrell.openbook.model.OpenBookMarket;
import com.mmorrell.openbook.model.OpenBookOpenOrdersAccount;
import com.mmorrell.openbook.program.OpenbookProgram;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.junit.Ignore;
import org.junit.Test;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ProgramAccount;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
public class OpenBookTest {

    private final RpcClient client = new RpcClient("https://mainnet.helius-rpc.com/?api-key=a778b653-bdd6-41bc-8cda-0c7377faf1dd");
    private final static byte[] MARKET_DISCRIMINATOR = {
            (byte) 0xDB, (byte) 0xBE, (byte) 0xD5, (byte) 0x37, (byte) 0x00, (byte) 0xE3,
            (byte) 0xC6, (byte) 0x9A
    };
    private final OpenBookManager openBookManager = new OpenBookManager(client);
    private static final String PRIVATE_KEY_FILE = "mikeDBaJgkicqhZcoYDBB4dRwZFFJCThtWCYD7A9FAH.json";

    @Test
    public void openBookV2Test() throws RpcException {
        final List<ProgramAccount> markets = client.getApi().getProgramAccountsBase64(
                OpenbookProgram.OPENBOOK_V2_PROGRAM_ID,
                0,
                Base58.encode(MARKET_DISCRIMINATOR)
        );

        log.info("# of OpenBook v2 markets: " + markets.size());
        markets.forEach(programAccount -> {
            OpenBookMarket openBookMarket = OpenBookMarket.readOpenBookMarket(
                    programAccount.getAccount().getDecodedData(),
                    new PublicKey(programAccount.getPubkey())
            );
            // log.info("Market: {}", openBookMarket);
            log.info("Market: {}, Name: {}", openBookMarket.getMarketId().toBase58(), openBookMarket.getName());
        });
    }

    @Test
    public void openBookGetMarketsTest() {
        log.info("Market cache: {}", openBookManager.getOpenBookMarkets());
    }

    @Test
    public void orderBookTest() throws RpcException, IOException {
        byte[] data = client.getApi().getAccountInfo(new PublicKey("DJY185dSMyF6TJZ61Gz1XhoQPWCEkpGAG5dmJmgUjhnQ"))
                .getDecodedData();

        BookSide bookSide = BookSide.readBookSide(data);
        log.info("SOL/USDC Bids: {}", bookSide);

//        Files.write(data, new File("fMeta.bin"));
        bookSide.getOrderTreeNodes().getNodes().stream()
                .filter(anyNode -> anyNode.getNodeTag() == NodeTag.LeafNode)
                .forEach(anyNode -> {
                    log.info("Leaf node: {}", anyNode);
                });
        log.info("Bids:");
        bookSide.getLeafNodes().stream().sorted(Comparator.comparingDouble(LeafNode::getPrice).reversed()).forEach(leafNode -> {
//            log.info("Leaf: {}", leafNode);
//            log.info("Hex: {}", ByteUtils.bytesToHex(leafNode.getKey()));
            log.info("Price: {}, Size: {}, Trader: {}", leafNode.getPrice(), leafNode.getQuantity(),
                    leafNode.getOwner());
        });
    }

    @Test
    public void getOBv2MarketWithBooksTest() {
        // SOL/USDC
        OpenBookMarket solUsdc = openBookManager.getMarket(
                PublicKey.valueOf("C3YPL3kYCSYKsmHcHrPWx1632GUXGqi2yMXJbfeCc57q"),
                false,
                true
        ).get();

        log.info("Bids: {}", solUsdc.getBidOrders());
        log.info("Asks: {}", solUsdc.getAskOrders());

        assertFalse(solUsdc.getBidOrders().isEmpty());
    }

    @Test
    @Ignore
    public void eventHeapTest() throws RpcException, IOException {
        byte[] data = client.getApi().getAccountInfo(new PublicKey("GY5HKym4yKNUpdHpBBiqLB3DHbrNKhLHDFTSLPK8AbFX"))
                .getDecodedData();
        Files.write(data, new File("eventHeap.bin"));
    }

    @Test
    public void openBookEventHeapTest() {
        // 2pMETA
        Optional<OpenBookEventHeap> eventHeap = openBookManager.getEventHeap(PublicKey.valueOf("5DviyqH9is6EwSjUETEh5XUe6xP9cpJu17cwiCuiGYQq"));
        //eventHeap.ifPresent(heap -> log.info("Event Heap: {}", heap));
        eventHeap.get().getFillEvents().forEach(openBookFillEvent -> {
            log.info("Fill: {}", openBookFillEvent.toString());
        });
    }

    @Test
    public void openBookEventHeapOutEventsTest() {
        // 2pMETA
        Optional<OpenBookEventHeap> eventHeap = openBookManager.getEventHeap(PublicKey.valueOf("GY5HKym4yKNUpdHpBBiqLB3DHbrNKhLHDFTSLPK8AbFX"));
        eventHeap.get().getOutEvents().forEach(openBookOutEvent -> {
            log.info("Out Event: {}", openBookOutEvent.toString());
        });
    }

    @Test
    public void openBookOpenOrdersAccountTest() throws RpcException, IOException {
        Optional<OpenBookOpenOrdersAccount> openBookOpenOrdersAccount = openBookManager.getOpenOrdersAccount(
                new PublicKey("G1hKFxyM3qNCd1nnjnuvydw6VjCowVp5Jm6w1mwyWH4r")
        );
        assertTrue(openBookOpenOrdersAccount.isPresent());
        OpenBookOpenOrdersAccount account = openBookOpenOrdersAccount.get();
        assertEquals(new PublicKey("7uixr2n3aawRYFKu5L6Wjwf37Fe6Twh6Ns3upAPq9H7k"), account.getOwner());

        byte[] data = client.getApi().getAccountInfo(new PublicKey("G1hKFxyM3qNCd1nnjnuvydw6VjCowVp5Jm6w1mwyWH4r"))
                .getDecodedData();
        Files.write(data, new File("ooa.bin"));
    }

    @Test
    public void consumeEventsTest() throws IOException, RpcException {
        Account tradingAccount = Account.fromJson(
                Resources.toString(Resources.getResource(PRIVATE_KEY_FILE), Charset.defaultCharset())
        );
        log.info("Account: {}", tradingAccount.getPublicKey().toBase58());

        OpenBookMarket solUsdc = openBookManager.getMarket(
                PublicKey.valueOf("5hYMkB5nAz9aJA33GizyPVH3VkqfkG7V4S2B5ykHxsiM"),
                true,
                false
        ).get();
//        for (OpenBookMarket solUsdc : openBookManager.getOpenBookMarkets()) {


        OpenBookEventHeap eventHeap = openBookManager.getEventHeap(
                solUsdc.getEventHeap()
        ).get();
        log.info("Market: {}", solUsdc.getName());
        log.info("Heap count: {}", eventHeap.getCount());

        List<PublicKey> peopleToCrank = new ArrayList<>();
        eventHeap.getFillEvents()
                .forEach(openBookFillEvent -> {
                    peopleToCrank.add(openBookFillEvent.getMaker());
                });

        log.info("Cranking {}: {}", solUsdc.getName(), peopleToCrank);

        Transaction tx = new Transaction();
        tx.addInstruction(
                OpenbookProgram.consumeEvents(
                        tradingAccount,
                        solUsdc.getMarketId(),
                        solUsdc.getEventHeap(),
                        peopleToCrank,
                        8
                )
        );


        String consumeEventsTx = new RpcClient(Cluster.MAINNET).getApi().sendTransaction(
                tx,
                List.of(tradingAccount),
                null
        );
        log.info("Consumed events in TX: {}", consumeEventsTx);
    }

    @Test
    public void consumeEventsAllMarketsTest() throws IOException, RpcException, InterruptedException {
        Account tradingAccount = Account.fromJson(
                Resources.toString(Resources.getResource(PRIVATE_KEY_FILE), Charset.defaultCharset())
        );
        log.info("Account: {}", tradingAccount.getPublicKey().toBase58());

        for (OpenBookMarket solUsdc : openBookManager.getOpenBookMarkets()) {
            OpenBookEventHeap eventHeap = openBookManager.getEventHeap(
                    solUsdc.getEventHeap()
            ).get();
            Thread.sleep(400);

            log.info("Market [Heap = {}]: {}", eventHeap.getCount(), solUsdc.getName());
            if (eventHeap.getCount() == 0) {
                continue;
            }

            List<PublicKey> peopleToCrank = new ArrayList<>();
            eventHeap.getFillEvents()
                    .forEach(openBookFillEvent -> {
                        peopleToCrank.add(openBookFillEvent.getMaker());
                    });

            log.info("Cranking {}: {}", solUsdc.getName(), peopleToCrank);

            Transaction tx = new Transaction();
            tx.addInstruction(
                    OpenbookProgram.consumeEvents(
                            tradingAccount,
                            solUsdc.getMarketId(),
                            solUsdc.getEventHeap(),
                            peopleToCrank,
                            8
                    )
            );


            String consumeEventsTx = new RpcClient(Cluster.MAINNET).getApi().sendTransaction(
                    tx,
                    List.of(tradingAccount),
                    null
            );
            log.info("Consumed events in TX: {}", consumeEventsTx);

            Thread.sleep(2000);
        }
    }

    @Test
    public void consumeEventsOpenBookManagerTest() throws IOException {
        Account tradingAccount = Account.fromJson(
                Resources.toString(Resources.getResource(PRIVATE_KEY_FILE), Charset.defaultCharset())
        );
        log.info("Account: {}", tradingAccount.getPublicKey().toBase58());

        Optional<String> transactionId = openBookManager.consumeEvents(
                tradingAccount,
                PublicKey.valueOf("C3YPL3kYCSYKsmHcHrPWx1632GUXGqi2yMXJbfeCc57q"),
                8
        );

        if (transactionId.isPresent()) {
            log.info("Cranked events: {}", transactionId.get());
        } else {
            log.info("No events found to consume.");
        }
    }
}
