package library.model;

import java.math.BigDecimal;

public class AudioBook extends LibraryItem {

    private static final int LOAN_DAYS = 14;
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("0.30");
    private final int durationMin;

    public AudioBook(String barcode, String isbn, String title, String author, int durationMin) {
        this(barcode, isbn, title, author, durationMin, ItemStatus.ON_SHELF);
    }
    public AudioBook(String barcode, String isbn, String title, String author, int durationMin, ItemStatus status) {
        super(barcode, isbn, title, author, status);
        this.durationMin = durationMin;
    }

    public int getDurationMin() {
        return durationMin;
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
