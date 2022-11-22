import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import core.Bank;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BankTransferManyThreadsTest {
    @Rule
    public ConcurrentRule concurrently = new ConcurrentRule();
    @Rule
    public RepeatingRule rule = new RepeatingRule();

    private static Bank sberBank = new Bank();
    private static long beforeTransactionBankBalance = sberBank.getSumAllAccounts();

    @Test
    @Concurrent(count = 10)
    @Repeating(repetition = 1000)
    public void runsMultipleTimes() {
        sberBank.transfer(sberBank.getAccountNumber(), sberBank.getAccountNumber(), sberBank.getAmount());
        sberBank.getBalance(sberBank.getAccountNumber());
    }

    @AfterClass
    public static void annotatedTestRunsMultipleTimes() {
        long afterTransactionBankBalance = sberBank.getSumAllAccounts();

        assertEquals(beforeTransactionBankBalance, afterTransactionBankBalance);
    }
}
