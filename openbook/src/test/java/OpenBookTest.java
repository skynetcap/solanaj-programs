import com.mmorrell.openbook.program.OpenbookProgram;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.util.List;

import static com.mmorrell.openbook.program.OpenbookProgram.OPENBOOK_V2_PROGRAM_ID;

@Slf4j
public class OpenBookTest {

    private final RpcClient rpcClient = new RpcClient(Cluster.MAINNET);
    private final Account testAccount = Account.fromJson("[255,181,248,82,179,247,83,90,145,105,170,35,64,239,41,71,160,49,254,240,122,128,229,155,52,230,53,193,23,246,83,29,11,116,195,237,133,152,251,78,144,10,130,201,91,66,43,143,101,167,69,56,134,4,6,47,31,100,212,108,71,177,73,156]");

    @Test
    public void openBookTest() throws Exception {
        log.info(testAccount.getPublicKey().toBase58());

        Transaction tx = new Transaction();
        tx.addInstruction(OpenbookProgram.createOpenOrdersIndexer(testAccount, SystemProgram.PROGRAM_ID));

        try {
            String txId = rpcClient.getApi().sendTransaction(tx, testAccount);
            log.info(txId);
        } catch (RpcException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void openBookCreateMarketTest() throws Exception {
        log.info(testAccount.getPublicKey().toBase58());

        // create base vault and create quote vault
        Account baseVault = new Account();
        Account quoteVault = new Account();


        // init base vault (wsol)
        Transaction baseVaultTx = new Transaction();
        baseVaultTx.addInstruction(
                SystemProgram.createAccount(
                        testAccount.getPublicKey(),
                        baseVault.getPublicKey(),
                        2039280L,
                        165L,
                        TokenProgram.PROGRAM_ID
                )
        );
        baseVaultTx.addInstruction(
                TokenProgram.initializeAccount(
                        baseVault.getPublicKey(),
                        new PublicKey("So11111111111111111111111111111111111111112"),
                        testAccount.getPublicKey()
                )
        );

        // init quote vault (usdc)
        baseVaultTx.addInstruction(
                SystemProgram.createAccount(
                        testAccount.getPublicKey(),
                        quoteVault.getPublicKey(),
                        2039280L,
                        165L,
                        TokenProgram.PROGRAM_ID
                )
        );
        baseVaultTx.addInstruction(
                TokenProgram.initializeAccount(
                        quoteVault.getPublicKey(),
                        new PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"),
                        testAccount.getPublicKey()
                )
        );

        final Account newMarketAccount = new Account();
//        baseVaultTx.addInstruction(
//                SystemProgram.createAccount(
//                        testAccount.getPublicKey(),
//                        newMarketAccount.getPublicKey(),
//                        8039280L,
//                        848L,
//                        OPENBOOK_V2_PROGRAM_ID
//                )
//        );

        try {
            String txId = rpcClient.getApi().sendTransaction(
                    baseVaultTx,
                    List.of(testAccount, baseVault, quoteVault, newMarketAccount),
                    null
            );
            log.info("Vault 1 and 2: " + txId);
        } catch (RpcException e) {
            throw new RuntimeException(e);
        }

        /// Market

        Transaction tx = new Transaction();
        tx.addInstruction(
                OpenbookProgram.createMarket(
                        testAccount,
                        newMarketAccount,
                        new PublicKey("So11111111111111111111111111111111111111112"),
                        new PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"),
                        baseVault.getPublicKey(),
                        quoteVault.getPublicKey()
                )
        );

        try {
            String txId = rpcClient.getApi().sendTransaction(tx, List.of(testAccount, newMarketAccount), null);
            log.info("Market: " + txId);
        } catch (RpcException e) {
            throw new RuntimeException(e);
        }

    }
}
