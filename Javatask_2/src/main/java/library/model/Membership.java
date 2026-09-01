package library.model;

public enum Membership {
    BASIC(3),
    PREMIUM(10);

    private final int maxLoans;

    Membership(int maxLoans) {
        this.maxLoans = maxLoans;
    }

    public int getMaxLoans() {
        return maxLoans;
    }
}
