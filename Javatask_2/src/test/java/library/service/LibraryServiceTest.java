package library.service;

import library.exception.ItemNotAvailableException;
import library.exception.ItemNotFoundException;
import library.exception.LoanLimitExceededException;
import library.exception.OutstandingFineException;
import library.model.ItemStatus;
import library.model.LibraryItem;
import library.model.Loan;
import library.model.Magazine;
import library.model.Member;
import library.model.Membership;
import library.model.PaperBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryServiceTest {

    private LibraryService service;

    @BeforeEach
    void setUp() {
        service = new LibraryService();
    }


    @Test
    void borrowingAnOnShelfPaperBookCreatesA21DayLoanAndMarksItOnLoan() throws Exception {
        PaperBook book = new PaperBook("B-0001", "ISBN-1", "The Hobbit", "J.R.R. Tolkien", 310);
        service.addItem(book);
        Member member = new Member("M-1", "Alice", Membership.BASIC);
        service.addMember(member);

        Loan loan = service.borrow("M-1", "B-0001");

        assertEquals(loan.getBorrowedOn().plusDays(21), loan.getDueOn());
        assertEquals(ItemStatus.ON_LOAN, book.getStatus());
    }


    @Test
    void borrowingAnItemThatIsOnLoanThrowsAndChangesNothing() throws Exception {
        PaperBook book = new PaperBook("B-0001", "ISBN-1", "The Hobbit", "J.R.R. Tolkien", 310);
        service.addItem(book);
        Member alice = new Member("M-1", "Alice", Membership.BASIC);
        Member bob = new Member("M-2", "Bob", Membership.BASIC);
        service.addMember(alice);
        service.addMember(bob);

        service.borrow("M-1", "B-0001");

        assertThrows(ItemNotAvailableException.class, () -> service.borrow("M-2", "B-0001"));
        assertEquals(ItemStatus.ON_LOAN, book.getStatus());
        assertEquals(1, service.loansByMember().get("M-1").size());
        assertTrue(service.loansByMember().get("M-2") == null);
    }


    @Test
    void basicMemberCannotExceedThreeActiveLoans() throws Exception {
        Member member = new Member("M-1", "Alice", Membership.BASIC);
        service.addMember(member);
        for (int i = 0; i < 3; i++) {
            service.addItem(new PaperBook("B-000" + i, "ISBN-" + i, "Title", "Author", 100));
            service.borrow("M-1", "B-000" + i);
        }
        service.addItem(new PaperBook("B-0099", "ISBN-99", "Title", "Author", 100));

        LoanLimitExceededException ex = assertThrows(LoanLimitExceededException.class,
                () -> service.borrow("M-1", "B-0099"));
        assertTrue(ex.getMessage().contains("3"));
        assertTrue(ex.getMessage().contains("M-1"));
    }


    @Test
    void outstandingFineBlocksBorrowingUntilPaidDown() throws Exception {
        Member member = new Member("M-1", "Alice", Membership.PREMIUM);
        service.addMember(member);
        service.addItem(new Magazine("B-0001", "ISBN-1", "Weekly", "Editor", 1));
        service.addItem(new Magazine("B-0002", "ISBN-2", "Weekly", "Editor", 2));
        Loan loan = service.borrow("M-1", "B-0001");
        service.returnItem(loan.getId(), loan.getDueOn().plusDays(25));
        OutstandingFineException ex = assertThrows(OutstandingFineException.class,
                () -> service.borrow("M-1", "B-0002"));
        assertTrue(ex.getMessage().contains("12.50"));
        service.payFine("M-1", member.getFineBalance());
        assertEquals(new BigDecimal("0.00"), member.getFineBalance());

        Loan retry = service.borrow("M-1", "B-0002");
        assertEquals("B-0002", retry.getBarcode());
    }


    @Test
    void lateReturnOfAMagazineChargesCorrectFineAndFreesTheItem() throws Exception {
        Member member = new Member("M-1", "Alice", Membership.BASIC);
        service.addMember(member);
        Magazine magazine = new Magazine("B-0001", "ISBN-1", "Weekly", "Editor", 1);
        service.addItem(magazine);

        Loan loan = service.borrow("M-1", "B-0001");
        BigDecimal fine = service.returnItem(loan.getId(), loan.getDueOn().plusDays(4));

        assertEquals(new BigDecimal("2.00"), fine);
        assertEquals(new BigDecimal("2.00"), member.getFineBalance());
        assertTrue(loan.getReturnedOn().isPresent());
        assertEquals(ItemStatus.ON_SHELF, magazine.getStatus());

        assertThrows(IllegalStateException.class,
                () -> service.returnItem(loan.getId(), loan.getDueOn().plusDays(4)));
    }


    @Test
    void onTimeReturnHasZeroFine() throws Exception {
        Member member = new Member("M-1", "Alice", Membership.BASIC);
        service.addMember(member);
        service.addItem(new PaperBook("B-0001", "ISBN-1", "Title", "Author", 100));

        Loan loan = service.borrow("M-1", "B-0001");
        BigDecimal fine = service.returnItem(loan.getId(), loan.getDueOn());

        assertEquals(new BigDecimal("0.00"), fine);
    }

    @Test
    void returnDateBeforeBorrowedOnIsRejected() throws Exception {
        Member member = new Member("M-1", "Alice", Membership.BASIC);
        service.addMember(member);
        service.addItem(new PaperBook("B-0001", "ISBN-1", "Title", "Author", 100));
        Loan loan = service.borrow("M-1", "B-0001");

        assertThrows(IllegalArgumentException.class,
                () -> service.returnItem(loan.getId(), loan.getBorrowedOn().minusDays(1)));
    }

    @Test
    void payFineRejectsZeroNegativeAndOverpayment() throws Exception {
        Member member = new Member("M-1", "Alice", Membership.BASIC);
        service.addMember(member);
        service.addItem(new Magazine("B-0001", "ISBN-1", "Weekly", "Editor", 1));
        Loan loan = service.borrow("M-1", "B-0001");
        service.returnItem(loan.getId(), loan.getDueOn().plusDays(2)); // fine = 1.00

        assertThrows(IllegalArgumentException.class, () -> service.payFine("M-1", BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> service.payFine("M-1", new BigDecimal("-1.00")));
        assertThrows(IllegalArgumentException.class, () -> service.payFine("M-1", new BigDecimal("5.00")));
    }


    @Test
    void availableCopiesExcludesLoanedCopiesAndSearchByAuthorIsCaseInsensitivePartial() throws Exception {
        PaperBook copy1 = new PaperBook("B-0001", "ISBN-1", "The Hobbit", "J.R.R. Tolkien", 310);
        PaperBook copy2 = new PaperBook("B-0002", "ISBN-1", "The Hobbit", "J.R.R. Tolkien", 310);
        PaperBook copy3 = new PaperBook("B-0003", "ISBN-1", "The Hobbit", "J.R.R. Tolkien", 310);
        service.addItem(copy1);
        service.addItem(copy2);
        service.addItem(copy3);
        Member member = new Member("M-1", "Alice", Membership.PREMIUM);
        service.addMember(member);
        service.borrow("M-1", "B-0001");
        service.borrow("M-1", "B-0002");

        List<LibraryItem> available = service.availableCopies("ISBN-1");
        assertEquals(1, available.size());
        assertEquals("B-0003", available.get(0).getBarcode());

        List<LibraryItem> matches = service.searchByAuthor("tolk");
        assertEquals(3, matches.size());
    }


    @Test
    void unknownBarcodeOrMemberIdThrowsItemNotFoundExceptionWithId() {
        service.addMember(new Member("M-1", "Alice", Membership.BASIC));
        service.addItem(new PaperBook("B-0001", "ISBN-1", "Title", "Author", 100));

        ItemNotFoundException unknownMember = assertThrows(ItemNotFoundException.class,
                () -> service.borrow("NOPE", "B-0001"));
        assertTrue(unknownMember.getMessage().contains("NOPE"));

        ItemNotFoundException unknownItem = assertThrows(ItemNotFoundException.class,
                () -> service.borrow("M-1", "NOPE"));
        assertTrue(unknownItem.getMessage().contains("NOPE"));
    }


    @Test
    void overdueLoansAreSortedByDaysLateDescendingAndLoansGroupByMember() throws Exception {
        Member member = new Member("M-1", "Alice", Membership.PREMIUM);
        service.addMember(member);
        service.addItem(new PaperBook("B-0001", "ISBN-1", "Title", "Author", 100));
        service.addItem(new PaperBook("B-0002", "ISBN-2", "Title", "Author", 100));

        Loan moreOverdue = service.borrowOn("M-1", "B-0001", LocalDate.now().minusDays(40));
        Loan lessOverdue = service.borrowOn("M-1", "B-0002", LocalDate.now().minusDays(30));

        List<Loan> overdue = service.overdueLoans();
        assertEquals(2, overdue.size());
        assertEquals(moreOverdue.getId(), overdue.get(0).getId());
        assertEquals(lessOverdue.getId(), overdue.get(1).getId());

        assertEquals(2, service.loansByMember().get("M-1").size());
    }


    @Test
    void returnedCollectionsAreReadOnly() throws Exception {
        Member member = new Member("M-1", "Alice", Membership.BASIC);
        service.addMember(member);
        PaperBook book = new PaperBook("B-0001", "ISBN-1", "Title", "Author", 100);
        service.addItem(book);
        service.borrow("M-1", "B-0001");

        assertThrows(UnsupportedOperationException.class,
                () -> service.searchByAuthor("Author").add(book));
        assertThrows(UnsupportedOperationException.class,
                () -> service.loansByMember().put("X", List.of()));
        assertThrows(UnsupportedOperationException.class,
                () -> service.loansByMember().get("M-1").add(null));
        assertThrows(UnsupportedOperationException.class,
                () -> service.overdueLoans().add(null));
    }
}
