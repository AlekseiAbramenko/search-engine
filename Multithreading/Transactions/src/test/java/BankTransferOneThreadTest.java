import core.Bank;
import junit.framework.TestCase;

public class BankTransferOneThreadTest extends TestCase {
    private static Bank sberBank = new Bank();

    @Override
    protected void setUp() {

    }

    public void testTransfer() {
        long beforeTransactionBankBalance = sberBank.getSumAllAccounts();

        for (int j = 0; j < 100; j++) {
           String accFrom = sberBank.getAccountNumber();
           String accTo = sberBank.getAccountNumber();
           int amount = sberBank.getAmount();

            sberBank.transfer(accFrom, accTo, amount);

            sberBank.getBalance(accFrom);
            sberBank.getBalance(accTo);
        }

        long afterTransactionBankBalance = sberBank.getSumAllAccounts();

        assertEquals(beforeTransactionBankBalance, afterTransactionBankBalance);
    }
}
