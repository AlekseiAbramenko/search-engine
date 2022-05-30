import java.util.Calendar;

public class DepositAccount extends BankAccount {

    private Calendar lastIncome;

    @Override
    public void put(double amountToPut) {
        super.put(amountToPut);
         lastIncome = Calendar.getInstance();
    }

    @Override
    public void take(double amountToTake) {
        Calendar takeDay = Calendar.getInstance();
        long daysFromLastIncome = (takeDay.getTimeInMillis()
                - lastIncome.getTimeInMillis()) / (24 * 60 * 60 * 1000);

        if (daysFromLastIncome > 30) {
            super.take(amountToTake);
        }
    }
}