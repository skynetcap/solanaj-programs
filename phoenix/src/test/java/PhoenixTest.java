import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.mmorrell.phoenix.model.FIFOOrderId;
import com.mmorrell.phoenix.model.FIFORestingOrder;
import com.mmorrell.phoenix.model.PhoenixMarket;
import com.mmorrell.phoenix.model.PhoenixMarketHeader;
import com.mmorrell.phoenix.program.PhoenixProgram;
import com.mmorrell.phoenix.program.PhoenixSeatManagerProgram;
import com.mmorrell.phoenix.util.Keccak;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Utils;
import org.junit.Test;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.ComputeBudgetProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.ProgramAccount;
import org.p2p.solanaj.rpc.types.config.Commitment;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class PhoenixTest {

    private final RpcClient client = new RpcClient("https://mainnet.helius-rpc.com/?api-key=a778b653-bdd6-41bc-8cda-0c7377faf1dd");
    private static final PublicKey SOL_USDC_MARKET = new PublicKey(
            "4DoNfFBfF7UokCC2FQzriy7yHK6DY6NVdYpuekQ5pRgg"
    );
    private static final PublicKey SOL_USDC_SEAT_MANAGER = new PublicKey(
            "JB3443UaUDA3z47AYdK4AUG8pgFgLfJVyyitHYkqC17L"
    );

    private static final PublicKey SOL_USDC_SEAT_DEPOSIT_COLLECTOR = new PublicKey(
            "DXECgdpNTDyXHGbyZfzLLriomtBvkXnBJipX3HgsyKXk"
    );

    @Test
    public void phoenixGetMarketsTest() throws RpcException {
        // GPA for all markets
        final List<ProgramAccount> markets = client.getApi().getProgramAccountsBase64(
                PhoenixProgram.PHOENIX_PROGRAM_ID,
                0,
                getDiscriminator("phoenix::program::accounts::MarketHeader")
        );

        System.out.println("Number of markets: " + markets.size());
        markets.forEach(programAccount -> {
            System.out.println("Market: " + programAccount.getPubkey());

            final PhoenixMarketHeader phoenixMarketHeader = PhoenixMarketHeader.readPhoenixMarketHeader(
                    Arrays.copyOfRange(
                            programAccount.getAccount().getDecodedData(),
                            0,
                            PhoenixMarketHeader.MARKET_HEADER_SIZE
                    )
            );
            System.out.println(phoenixMarketHeader);

        });
    }

    @Test
    public void phoenixGetMarketDetailTest() throws RpcException, IOException {
        final AccountInfo marketAccountInfo = client.getApi().getAccountInfo(
                SOL_USDC_MARKET,
                Map.of("commitment", Commitment.PROCESSED)
        );

        Files.write(marketAccountInfo.getDecodedData(), new File("phoenixMarket.bin"));

        byte[] data = marketAccountInfo.getDecodedData();
        // 576 = start of pub _padding: [u64; 32],v for market struct
        int marketStartOffset = 576;
        int baseLotsPerBaseUnitOffset = 832; // start at base lots to ignore padding
        // pub tick_size_in_quote_lots_per_base_unit: u64,
        int tickSizeInQuoteLotsPerBaseUnitOffset = baseLotsPerBaseUnitOffset + 8;

        log.info("Market start: " + marketStartOffset);
        log.info("Base per base unit offset: " + baseLotsPerBaseUnitOffset);
        log.info("Market detail length: {}", marketAccountInfo.getDecodedData().length);

        // Deserialization

        long baseLotsPerBaseUnit = Utils.readInt64(data, baseLotsPerBaseUnitOffset);
        long tickSizeInQuoteLotsPerBaseUnit = Utils.readInt64(data, tickSizeInQuoteLotsPerBaseUnitOffset);

        log.info("Base lots per base unit: {}", baseLotsPerBaseUnit);
        log.info("Tick size in quote lots per base unit: {}", tickSizeInQuoteLotsPerBaseUnit);

        PhoenixMarketHeader header = PhoenixMarketHeader.readPhoenixMarketHeader(data);

        PhoenixMarket phoenixMarket = PhoenixMarket.readPhoenixMarket(data, header);
        log.info("Phoenix market: {}", phoenixMarket.toString());
        log.info("Header from market: {}", header.toString());
        log.info("Bids size: {}, Asks Size: {}, Number of seats: {}", header.getBidsSize(), header.getAsksSize(),
                header.getNumSeats());

//        var sortedList = phoenixMarket.getBidOrders().entrySet().stream().sorted(
//                        (o1, o2) -> Math.toIntExact(o2.getKey().getPriceInTicks() - o1.getKey().getPriceInTicks())
//                )
//                .collect(Collectors.toList());
//
//        log.info("Top Bids: {}", sortedList);
//
//        sortedList.forEach(fifoOrderIdFIFORestingOrderEntry -> {
//            log.info(String.format("Price: %.2f, Size: %.2f",
//                    (double) fifoOrderIdFIFORestingOrderEntry.getKey().getPriceInTicks() / phoenixMarket.getTickSizeInQuoteLotsPerBaseUnit(),
//                    (double) fifoOrderIdFIFORestingOrderEntry.getValue().getNumBaseLots() / phoenixMarket.getBaseLotsPerBaseUnit()));
//        });
//
//        var sortedListSanitized = phoenixMarket.getBidOrdersSanitized().entrySet().stream().sorted(
//                        (o1, o2) -> Math.toIntExact(o2.getKey().getPriceInTicks() - o1.getKey().getPriceInTicks())
//                )
//                .collect(Collectors.toList());
//
//        log.info("Top Bids Sanitized: {}", sortedListSanitized);
//
//        sortedListSanitized.forEach(fifoOrderIdFIFORestingOrderEntry -> {
//            log.info(String.format("Price: %.2f, Size: %.2f",
//                    (double) fifoOrderIdFIFORestingOrderEntry.getKey().getPriceInTicks() / phoenixMarket.getTickSizeInQuoteLotsPerBaseUnit(),
//                    (double) fifoOrderIdFIFORestingOrderEntry.getValue().getNumBaseLots() / phoenixMarket.getBaseLotsPerBaseUnit()));
//        });

        var bids = phoenixMarket.getBidListSanitized().stream().sorted(
                (o1, o2) -> Math.toIntExact(o2.component1().getPriceInTicks() - o1.getFirst().getPriceInTicks())
        ).toList();
        bids.forEach(fifoOrderIdFIFORestingOrderPair -> {
            log.info(String.format("Bid: $%.2f, Size: %.2f SOL",
                    (double) fifoOrderIdFIFORestingOrderPair.getFirst().getPriceInTicks() / phoenixMarket.getTickSizeInQuoteLotsPerBaseUnit(),
                    (double) fifoOrderIdFIFORestingOrderPair.getSecond().getNumBaseLots() / phoenixMarket.getBaseLotsPerBaseUnit()));
        });

        var asks = phoenixMarket.getAskListSanitized().stream().sorted(
                (o1, o2) -> Math.toIntExact(o1.component1().getPriceInTicks() - o2.getFirst().getPriceInTicks())
        ).toList();
        asks.forEach(fifoOrderIdFIFORestingOrderPair -> {
            log.info(String.format("Ask: $%.2f, Size: %.2f SOL",
                    (double) fifoOrderIdFIFORestingOrderPair.getFirst().getPriceInTicks() / phoenixMarket.getTickSizeInQuoteLotsPerBaseUnit(),
                    (double) fifoOrderIdFIFORestingOrderPair.getSecond().getNumBaseLots() / phoenixMarket.getBaseLotsPerBaseUnit()));
        });

        var traders = phoenixMarket.getTraders();
        traders.forEach((publicKey, phoenixTraderState) -> {
            log.info("Trader Pubkey: {}, State: {}", publicKey.toBase58(), phoenixTraderState.toString());
        });
    }

    @Test
    public void phoenixClaimSeatTest() throws RpcException, IOException {
        Account tradingAccount = Account.fromJson(
                Resources.toString(
                        Resources.getResource(
                                "mikefsWLEcNYHgsiwSRr6PVd7yVcoKeaURQqeDE1tXN.json"),
                        Charset.defaultCharset()
                )
        );
        log.info("Trading account: {}", tradingAccount.getPublicKey().toBase58());

        // Claim Seat
        Transaction claimSeatTransaction = new Transaction();

        claimSeatTransaction.addInstruction(
                ComputeBudgetProgram.setComputeUnitPrice(
                        1_000_000
                )
        );

        claimSeatTransaction.addInstruction(
                ComputeBudgetProgram.setComputeUnitLimit(
                        200_000
                )
        );

        claimSeatTransaction.addInstruction(
                PhoenixSeatManagerProgram.claimSeat(
                        SOL_USDC_MARKET,
                        SOL_USDC_SEAT_MANAGER,
                        SOL_USDC_SEAT_DEPOSIT_COLLECTOR,
                        tradingAccount.getPublicKey(),
                        tradingAccount.getPublicKey()
                )
        );

        String claimSeatTxId = client.getApi().sendTransaction(
                claimSeatTransaction,
                List.of(tradingAccount),
                client.getApi().getRecentBlockhash(Commitment.PROCESSED)
        );
        log.info("Claimed seat in transaction: {}", claimSeatTxId);
    }

    private String getDiscriminator(String input) {
        Keccak keccak = new Keccak(256);
        keccak.update(PhoenixProgram.PHOENIX_PROGRAM_ID.toByteArray());
        keccak.update(input.getBytes());

        ByteBuffer keccakBuffer = keccak.digest();
        keccakBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] keccakBytes = keccakBuffer.array();

        return Base58.encode(Arrays.copyOfRange(keccakBytes, 0, 8));
    }
}