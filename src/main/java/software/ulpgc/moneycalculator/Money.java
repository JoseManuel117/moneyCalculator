package software.ulpgc.moneycalculator;

public record Money(double amount, Currency currency) {
    @Override
    public String toString() {
        return amount + " " + currency;
    }
}
