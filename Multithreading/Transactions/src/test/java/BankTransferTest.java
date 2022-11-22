import core.Bank;
import junit.framework.TestCase;

public class BankTransferTest extends TestCase {

   private Bank sberBank = new Bank();
   private final String richAcc = sberBank.getAccountNumber();
   private final String poorAcc = sberBank.getAccountNumber();
   private final int amount = sberBank.getAmount();


    @Override
    protected void setUp() {

    }

    public void testTransfer() {
        long beforeTransactionBankBalance = sberBank.getSumAllAccounts();
        long expectedPoor = sberBank.getAccounts().get(poorAcc).getMoney() + amount;
        long expectedRich = sberBank.getAccounts().get(richAcc).getMoney() - amount;

        sberBank.transfer(richAcc, poorAcc, amount);

        long afterTransactionBankBalance = sberBank.getSumAllAccounts();
        long actualPoor = sberBank.getAccounts().get(poorAcc).getMoney();
        long actualRich = sberBank.getAccounts().get(richAcc).getMoney();

        assertEquals(expectedPoor, actualPoor);
        assertEquals(expectedRich, actualRich);
        assertEquals(beforeTransactionBankBalance, afterTransactionBankBalance);
    }
}
