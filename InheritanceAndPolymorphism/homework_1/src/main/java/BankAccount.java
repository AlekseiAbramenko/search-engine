public class BankAccount {

  protected double amount = 0.00;

  protected double getAmount() {
    return amount;
  }

  protected void put(double amountToPut) {
    if (amountToPut > 0) {
      amount += amountToPut;
    }
  }

  protected void take(double amountToTake) {
    if (amountToTake <= amount) {
      amount -= amountToTake;
    }
  }
}
