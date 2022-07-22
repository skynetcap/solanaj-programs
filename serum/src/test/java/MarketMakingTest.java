import ch.openserum.serum.manager.SerumManager;
import ch.openserum.serum.model.*;
import ch.openserum.serum.program.SerumProgram;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Market make ORCA-USDC on Serum
 *
 * 20% bid-ask spread (10% from midpoint in each direction)
 *
 * Rebalance against coingecko
 *
 * Adjust deltas as quotes are lifted
 */
public class MarketMakingTest {

    private final RpcClient client = new RpcClient("https://ssc-dao.genesysgo.net/");
    private final SerumManager serumManager = new SerumManager(client);
    private final OkHttpClient httpClient = new OkHttpClient();

    private final PublicKey orcaMarket = PublicKey.valueOf("8N1KkhaCYDpj3awD58d85n973EwkpeYnRp84y1kdZpMX");
    private final PublicKey orcaWallet = PublicKey.valueOf("BgLAeRn8nt2APbCerWyYvGrGUDoRxT4o2FYP51MgX6Kn");
    private final PublicKey usdcWallet = PublicKey.valueOf("5yHduya2yKQdZFPU4rTfi4cRG8M5tjK3wcVRSZ6CnafP");
    private final PublicKey openOrdersAccount = PublicKey.valueOf("5QNcrySBPvkVZhKZejGkneduCN2dJa5YHCqPhvo6ubDz");

    int bidAmountUsdc = 30;
    int askAmountOrca = 30;

    // https://api.coingecko.com/api/v3/simple/price?ids=orca&vs_currencies=usd
    // {"orca":{"usd":0.934155}}

    @Test
    public void orcaMm() throws IOException, InterruptedException {
        Account account = Account.fromJson(Files.readString(Paths.get("src/test/resources/mainnet.json")));

        float orcaPriceCoingecko = getOrcaPriceCoingecko();
        System.out.println("ORCA Price (Coingecko): " + orcaPriceCoingecko);

        final Market market = new MarketBuilder()
                .setPublicKey(orcaMarket)
                .setClient(client)
                .setRetrieveOrderBooks(true)
                .build();

        // cancel any open orders
        final Transaction cancelTransaction = new Transaction();
        cancelTransaction.addInstruction(
                SerumProgram.cancelOrderByClientId(
                        market,
                        openOrdersAccount,
                        account.getPublicKey(),
                        11133711L
                )
        );

        cancelTransaction.addInstruction(
                SerumProgram.cancelOrderByClientId(
                        market,
                        openOrdersAccount,
                        account.getPublicKey(),
                        1142011L
                )
        );
        try {
            client.getApi().sendTransaction(cancelTransaction, account);
            System.out.println("Orders successfully cancelled.");
        } catch (RpcException ex) {
            System.out.println("No orders to cancel.");
        }

        boolean ordersInitialized = false;
        while (orcaPriceCoingecko > 0.0f) {
            final Transaction transaction = new Transaction();

            if (ordersInitialized) {
                transaction.addInstruction(
                        SerumProgram.cancelOrderByClientId(
                                market,
                                openOrdersAccount,
                                account.getPublicKey(),
                                11133711L
                        )
                );

                transaction.addInstruction(
                        SerumProgram.cancelOrderByClientId(
                                market,
                                openOrdersAccount,
                                account.getPublicKey(),
                                1142011L
                        )
                );
            }

            transaction.addInstruction(
                    SerumProgram.consumeEvents(
                            account.getPublicKey(),
                            List.of(
                                    openOrdersAccount
                            ),
                            market,
                            orcaWallet,
                            usdcWallet
                    )
            );

            transaction.addInstruction(
                    SerumProgram.settleFunds(
                            market,
                            openOrdersAccount,
                            account.getPublicKey(),
                            orcaWallet,
                            usdcWallet
                    )
            );

            float bidPrice = orcaPriceCoingecko * .93f;
            float askPrice = orcaPriceCoingecko * 1.07f;

            final Order bidSide = Order.builder()
                    .floatPrice(bidPrice)
                    .floatQuantity(bidAmountUsdc)
                    .clientOrderId(11133711L)
                    .orderTypeLayout(OrderTypeLayout.LIMIT)
                    .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                    .buy(true)
                    .build();

            serumManager.setOrderPrices(bidSide, market);
            transaction.addInstruction(
                    SerumProgram.placeOrder(
                            account,
                            usdcWallet,
                            openOrdersAccount,
                            market,
                            bidSide
                    )
            );

            final Order askSide = Order.builder()
                    .floatPrice(askPrice)
                    .floatQuantity(askAmountOrca)
                    .clientOrderId(1142011L)
                    .orderTypeLayout(OrderTypeLayout.LIMIT)
                    .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                    .buy(false)
                    .build();

            serumManager.setOrderPrices(askSide, market);
            transaction.addInstruction(
                    SerumProgram.placeOrder(
                            account,
                            orcaWallet,
                            openOrdersAccount,
                            market,
                            askSide
                    )
            );

            transaction.addInstruction(
                    MemoProgram.writeUtf8(
                            account.getPublicKey(),
                            "Liquidity by openserum.io"
                    )
            );

            System.out.printf("Placing bids: %d @ %.4f, ask: %d @ %.4f, time = %d%n", bidAmountUsdc, bidPrice,
                    askAmountOrca, askPrice, System.currentTimeMillis());
            try {
                System.out.println("TX: " + client.getApi().sendTransaction(transaction, account));
            } catch (RpcException ex) {
                System.out.println("Error: " + ex.getMessage());
            }

            ordersInitialized = true;

            Thread.sleep(10000L);
            orcaPriceCoingecko = getOrcaPriceCoingecko();
        }

    }

    private float getOrcaPriceCoingecko() throws IOException {
        Request request = new Request.Builder()
                .url("https://api.coingecko.com/api/v3/simple/price?ids=orca&vs_currencies=usd")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String price = response.body().string().replace("{\"orca\":{\"usd\":", "").replace("}}", "");
            return Float.parseFloat(price);
        }
    }

}
