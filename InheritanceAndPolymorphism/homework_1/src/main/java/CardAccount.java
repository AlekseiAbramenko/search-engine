public class CardAccount extends BankAccount {

    @Override
    public void take(double amountToTake) {
        double cashWithdrawalFee = 0.01;
        super.take(amountToTake + amountToTake * cashWithdrawalFee);
    }
}
