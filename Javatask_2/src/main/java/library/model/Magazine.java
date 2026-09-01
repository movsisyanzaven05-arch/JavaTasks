package library.model;

import java.math.BigDecimal;

public class Magazine extends LibraryItem {

    private static final int LOAN_DAYS = 7;
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("0.50");

    private final int issueNumber;

    public Magazine(String barcode, String isbn, String title, String author, int issueNumber) {
        this(barcode, isbn, title, author, issueNumber, ItemStatus.ON_SHELF);
    }

    public Magazine(String barcode, String isbn, String title, String author, int issueNumber, ItemStatus status) {
        super(barcode, isbn, title, author, status);
        this.issueNumber = issueNumber;
    }

    public int getIssueNumber() {
        return issueNumber;
    }

    @Override
    public int loanDays() {
        return LOAN_DAYS;
    }

    @Override
    public BigDecimal finePerDay() {
        return FINE_PER_DAY;
    }
}
