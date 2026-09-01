package library.model;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class LibraryItem {

    private final String barcode;
    private final String isbn;
    private final String title;
    private final String author;
    private ItemStatus status;

    protected LibraryItem(String barcode, String isbn, String title, String author, ItemStatus status) {
        this.barcode = barcode;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.status = status == null ? ItemStatus.ON_SHELF : status;
    }

    public abstract int loanDays();
    public abstract BigDecimal finePerDay();
    public String getBarcode() {
        return barcode;
    }
    public String getIsbn() {
        return isbn;
    }
    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    public ItemStatus getStatus() {
        return status;
    }
    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LibraryItem)) return false;
        LibraryItem that = (LibraryItem) o;
        return barcode.equals(that.barcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(barcode);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{barcode='" + barcode + "', isbn='" + isbn
                + "', title='" + title + "', author='" + author + "', status=" + status + "}";
    }
}
