import library.exception.ItemNotAvailableException;
import library.exception.ItemNotFoundException;
import library.exception.LoanLimitExceededException;
import library.exception.OutstandingFineException;
import library.model.AudioBook;
import library.model.LibraryItem;
import library.model.Loan;
import library.model.Magazine;
import library.model.Member;
import library.model.Membership;
import library.model.PaperBook;
import library.service.LibraryService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class Main {

    public static void main(String[] args) {
        LibraryService service = new LibraryService();

        PaperBook hobbit1 = new PaperBook("B-0001", "ISBN-001", "The Hobbit", "J.R.R. Tolkien", 310);
        PaperBook hobbit2 = new PaperBook("B-0002", "ISBN-001", "The Hobbit", "J.R.R. Tolkien", 310);
        PaperBook hobbit3 = new PaperBook("B-0003", "ISBN-001", "The Hobbit", "J.R.R. Tolkien", 310);
        AudioBook dune = new AudioBook("B-0004", "ISBN-002", "Dune", "Frank Herbert", 1260);
        Magazine natGeo = new Magazine("B-0005", "ISBN-003", "National Geographic", "Various", 220);

        for (LibraryItem item : List.of(hobbit1, hobbit2, hobbit3, dune, natGeo)) {
            service.addItem(item);
        }

        Member alice = new Member("M-001", "Alice", Membership.BASIC);
        Member bob = new Member("M-002", "Bob", Membership.PREMIUM);
        service.addMember(alice);
        service.addMember(bob);

        System.out.println("AC1: borrow a paper book");
        try {
            Loan loan1 = service.borrow("M-001", "B-0001");
            System.out.println("Borrowed: " + loan1);
            System.out.println("Item status: " + hobbit1.getStatus());
        } catch (ItemNotAvailableException | LoanLimitExceededException | OutstandingFineException e) {
            System.out.println("Unexpected: " + e.getMessage());
        }

        System.out.println();
        System.out.println("AC2: borrow an item already on loan");
        try {
            service.borrow("M-002", "B-0001");
        } catch (ItemNotAvailableException e) {
            System.out.println("Rejected as expected: " + e.getMessage());
        } catch (LoanLimitExceededException | OutstandingFineException e) {
            System.out.println("Unexpected: " + e.getMessage());
        }

        System.out.println();
        System.out.println("AC3: loan limit for a BASIC member");
        try {
            service.borrow("M-001", "B-0004");
            service.borrow("M-001", "B-0005");
            // Alice now has 3 active loans (limit for BASIC); a 4th must fail.
            service.borrow("M-001", "B-0002");
        } catch (LoanLimitExceededException e) {
            System.out.println("Rejected as expected: " + e.getMessage());
        } catch (ItemNotAvailableException | OutstandingFineException e) {
            System.out.println("Unexpected: " + e.getMessage());
        }

        System.out.println();
        System.out.println("AC5 / AC6: return with and without a fine");
        try {
            Loan magazineLoan = service.loansByMember().get("M-001").stream()
                    .filter(l -> l.getBarcode().equals("B-0005"))
                    .findFirst()
                    .orElseThrow();
            LocalDate lateReturn = magazineLoan.getDueOn().plusDays(4);
            BigDecimal fine = service.returnItem(magazineLoan.getId(), lateReturn);
            System.out.println("Fine for 4 days late on a magazine: " + fine);
            System.out.println("Anis balance: " + alice.getFineBalance());

            try {
                service.returnItem(magazineLoan.getId(), lateReturn);
            } catch (IllegalStateException e) {
                System.out.println("Second return rejected as expected: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Unexpected: " + e.getMessage());
        }

        System.out.println();
        System.out.println("AC4: outstanding fine blocks borrowing");
        try {
            Loan audioLoan = service.loansByMember().get("M-001").stream()
                    .filter(l -> l.getBarcode().equals("B-0004"))
                    .findFirst()
                    .orElseThrow();
            LocalDate veryLate = audioLoan.getDueOn().plusDays(40); // 0.30 * 40 = 12.00
            BigDecimal fine = service.returnItem(audioLoan.getId(), veryLate);
            System.out.println("Fine for 40 days late on an audiobook: " + fine);
            System.out.println("Ani's balance: " + alice.getFineBalance());

            try {
                service.borrow("M-001", "B-0002");
            } catch (OutstandingFineException e) {
                System.out.println("Rejected as expected: " + e.getMessage());
            }

            service.payFine("M-001", alice.getFineBalance());
            System.out.println("Ani's balance after paying in full: " + alice.getFineBalance());

            Loan retryLoan = service.borrow("M-001", "B-0002");
            System.out.println("Borrow succeeded after payment: " + retryLoan);
        } catch (Exception e) {
            System.out.println("Unexpected: " + e.getMessage());
        }

        System.out.println();
        System.out.println("AC7: available copies and author search");
        System.out.println("Available copies of ISBN-001: " + service.availableCopies("ISBN-001"));
        System.out.println("Search author 'tolk': " + service.searchByAuthor("tolk"));

        System.out.println();
        System.out.println("AC8: unknown barcode / member id");
        try {
            service.borrow("M-999", "B-0001");
        } catch (ItemNotFoundException e) {
            System.out.println("Rejected as expected: " + e.getMessage());
        } catch (ItemNotAvailableException | LoanLimitExceededException | OutstandingFineException e) {
            System.out.println("Unexpected: " + e.getMessage());
        }
        try {
            service.borrow("M-001", "B-9999");
        } catch (ItemNotFoundException e) {
            System.out.println("Rejected as expected: " + e.getMessage());
        } catch (ItemNotAvailableException | LoanLimitExceededException | OutstandingFineException e) {
            System.out.println("Unexpected: " + e.getMessage());
        }

        System.out.println();
        System.out.println("AC9: overdue loans and loans grouped by member");
        try {
            service.borrowOn("M-002", "B-0005", LocalDate.now().minusDays(30));
        } catch (Exception e) {
            System.out.println("Unexpected: " + e.getMessage());
        }
        System.out.println("Overdue loans: " + service.overdueLoans());
        Map<String, List<Loan>> byMember = service.loansByMember();
        byMember.forEach((memberId, memberLoans) -> System.out.println(memberId + " -> " + memberLoans));

        System.out.println();
        System.out.println("AC10: returned collections are read-only");
        try {
            service.availableCopies("ISBN-001").add(hobbit1);
        } catch (UnsupportedOperationException e) {
            System.out.println("availableCopies() list is immutable, as expected");
        }
        try {
            service.loansByMember().put("X", List.of());
        } catch (UnsupportedOperationException e) {
            System.out.println("loansByMember() map is immutable, as expected");
        }
    }

    private Main() {
    }
}
