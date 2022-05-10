import com.skynet.auctionhouse.program.AuctionHouseProgram;
import org.junit.Ignore;
import org.junit.Test;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

public class AuctionHouseTest {

    private static final Logger LOGGER = Logger.getLogger(AuctionHouseTest.class.getName());
    private final RpcClient client = new RpcClient(Cluster.MAINNET);

    private static final PublicKey METAPLEX_AUCTION_HOUSE_PROGRAM_ID = PublicKey
            .valueOf("hausS13jsjafwWwGqZTUQRmWyvyxn9EQpqMwV1PBBmk");

    private static final PublicKey METAPLEX_AUCTION_HOUSE_FEE_ACCOUNT = PublicKey
            .valueOf("5xyoD5hnWDBLsW6rjW2TBmqiDQsbod6Uaw15dRhNrDzn");

    @Test
    @Ignore
    public void testSell() {
        // Create account from private key
        Account account = null;
        try {
            account = Account.fromJson(Files.readString(Paths.get("src/test/resources/mainnet.json")));
            LOGGER.info("our pubkey = " + account.getPublicKey());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (account == null) {
            return;
        }

        final Transaction transaction = new Transaction();
        // Sell instruction
        transaction.addInstruction(
                AuctionHouseProgram.sell(
                        account.getPublicKey(),
                        PublicKey.valueOf("CghEDimCSVhKJsNQ9Ujwp5WXVU2cYJdxVidVWFh9Rv5T"), // my solsunset
                        PublicKey.valueOf("7H8NfDpgGtG1C81WGzYa8hSE7dWLxHUnQdksuN24bkYR"), // sunset metadata
                        PublicKey.valueOf("pAHAKoTJsAAe2ZcvTZUxoYzuygVAFAmbYmJYdWT886r"), // authority (fixed)
                        PublicKey.valueOf("3o9d13qUvEuuauhFrVom1vuCzgNsJifeaBYDPquaT73Y"), // ah (fixed)
                        METAPLEX_AUCTION_HOUSE_FEE_ACCOUNT,
                        new Account().getPublicKey(), // seller trade state (?)
                        new Account().getPublicKey(), // free seller trade state(?)
                        PublicKey.valueOf("HS2eL9WJbh7pA4i4veK3YDwhGLRjY3uKryvG1NbHRprj"),
                        (byte) 251,
                        (byte) 255,
                        (byte) 255,
                        1337000000,
                        1
                )
        );

        // Pay auction house fee instruction
        transaction.addInstruction(
                AuctionHouseProgram.payAuctionHouseFeeAccount(
                        account.getPublicKey(),
                        METAPLEX_AUCTION_HOUSE_FEE_ACCOUNT
                )
        );
//
//        transaction.addInstruction(
//                MemoProgram.writeUtf8(
//                        account.getPublicKey(),
//                        "If you're reading this it's too late. - SolanaJ"
//                )
//        );

        // Call sendTransaction
        String result;
        try {
            result = client.getApi().sendTransaction(transaction, account);
            LOGGER.info("Result = " + result);
        } catch (RpcException e) {
            e.printStackTrace();
        }
    }


}
