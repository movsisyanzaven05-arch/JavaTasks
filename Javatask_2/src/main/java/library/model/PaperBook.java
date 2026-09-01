package library.model;

import java.math.BigDecimal;

public class PaperBook extends LibraryItem {
    private static final int LOAN_DAYS = 21;
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("0.20");
    private final int pages;

    public PaperBook(String barcode, String isbn, String title, String author, int pages) {
        this(barcode, isbn, title, author, pages, ItemStatus.ON_SHELF);
    }

    public PaperBook(String barcode, String isbn, String title, String author, int pages, ItemStatus status) {
        super(barcode, isbn, title, author, status);
        this.pages = pages;
    }

    public int getPages() {
        return pages;
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
