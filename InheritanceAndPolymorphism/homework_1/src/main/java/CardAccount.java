public class CardAccount extends BankAccount {

    private final double cashWithdrawalFee = 0.01;

    @Override
    public void take(double amountToTake) {
        super.take(amountToTake + amountToTake * cashWithdrawalFee);
    }
}
