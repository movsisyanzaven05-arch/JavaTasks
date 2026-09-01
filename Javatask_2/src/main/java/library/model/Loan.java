package library.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public final class Loan {

    private final String id;
    private final String barcode;
    private final String memberId;
    private final LocalDate borrowedOn;
    private final LocalDate dueOn;
    private LocalDate returnedOn;

    public Loan(String id, String barcode, String memberId, LocalDate borrowedOn, LocalDate dueOn) {
        this.id = id;
        this.barcode = barcode;
        this.memberId = memberId;
        this.borrowedOn = borrowedOn;
        this.dueOn = dueOn;
        this.returnedOn = null;
    }

    public String getId() {
        return id;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getMemberId() {
        return memberId;
    }

    public LocalDate getBorrowedOn() {
        return borrowedOn;
    }

    public LocalDate getDueOn() {
        return dueOn;
    }

    public Optional<LocalDate> getReturnedOn() {
        return Optional.ofNullable(returnedOn);
    }

    public boolean isActive() {
        return returnedOn == null;
    }

    public void markReturned(LocalDate returnedOn) {
        if (this.returnedOn != null) {
            throw new IllegalStateException("loan " + id + " has already been returned");
        }
        this.returnedOn = returnedOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loan)) return false;
        Loan loan = (Loan) o;
        return id.equals(loan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Loan{id='" + id + "', barcode='" + barcode + "', memberId='" + memberId
                + "', borrowedOn=" + borrowedOn + ", dueOn=" + dueOn + ", returnedOn=" + returnedOn + "}";
    }
}
