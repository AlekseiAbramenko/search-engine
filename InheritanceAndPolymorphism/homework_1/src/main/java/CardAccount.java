public class CardAccount extends BankAccount {

    @Override
    public void take(double amountToTake) {
        double cashWithdrawalFee = 0.01;

        if (amountToTake + amountToTake * cashWithdrawalFee < amount) {
            amount -= (amountToTake + amountToTake * cashWithdrawalFee);
        }
    }
}
