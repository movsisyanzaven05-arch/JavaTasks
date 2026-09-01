package library.service;

import library.exception.ItemNotAvailableException;
import library.exception.ItemNotFoundException;
import library.exception.LoanLimitExceededException;
import library.exception.OutstandingFineException;
import library.model.ItemStatus;
import library.model.LibraryItem;
import library.model.Loan;
import library.model.Member;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class LibraryService {
    private static final BigDecimal MAX_ALLOWED_BALANCE = new BigDecimal("10.00");
    private final Map<String, LibraryItem> items = new LinkedHashMap<>();
    private final Map<String, Member> members = new LinkedHashMap<>();
    private final List<Loan> loans = new ArrayList<>();
    private final AtomicLong loanSequence = new AtomicLong(1);
    public void addItem(LibraryItem item) {
        if (item == null) {
            throw new IllegalArgumentException("item must not be null");
        }
        if (items.containsKey(item.getBarcode())) {
            throw new IllegalArgumentException("an item with barcode '" + item.getBarcode() + "' already exists");
        }
        items.put(item.getBarcode(), item);
    }

    public void addMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member must not be null");
        }
        if (members.containsKey(member.getId())) {
            throw new IllegalArgumentException("a member with id '" + member.getId() + "' already exists");
        }
        members.put(member.getId(), member);
    }
    public Loan borrow(String memberId, String barcode)
            throws ItemNotAvailableException, LoanLimitExceededException, OutstandingFineException {
        return borrowOn(memberId, barcode, LocalDate.now());
    }
    public Loan borrowOn(String memberId, String barcode, LocalDate borrowedOn)
            throws ItemNotAvailableException, LoanLimitExceededException, OutstandingFineException {
        requireNonBlank(memberId, "memberId");
        requireNonBlank(barcode, "barcode");
        if (borrowedOn == null) {
            throw new IllegalArgumentException("borrowedOn must not be null");
        }

        Member member = findMember(memberId);
        LibraryItem item = findItem(barcode);

        if (item.getStatus() != ItemStatus.ON_SHELF) {
            throw new ItemNotAvailableException(barcode);
        }

        if (member.getFineBalance().compareTo(MAX_ALLOWED_BALANCE) > 0) {
            throw new OutstandingFineException(memberId, member.getFineBalance());
        }

        int activeLoanCount = (int) loans.stream()
                .filter(loan -> loan.getMemberId().equals(memberId))
                .filter(Loan::isActive)
                .count();
        int limit = member.getMembership().getMaxLoans();
        if (activeLoanCount >= limit) {
            throw new LoanLimitExceededException(memberId, limit);
        }

        LocalDate dueOn = borrowedOn.plusDays(item.loanDays());
        Loan loan = new Loan(nextLoanId(), barcode, memberId, borrowedOn, dueOn);

        item.setStatus(ItemStatus.ON_LOAN);
        loans.add(loan);
        return loan;
    }

    public BigDecimal returnItem(String loanId, LocalDate returnDate) {
        if (loanId == null || loanId.isBlank()) {
            throw new IllegalArgumentException("loanId must not be blank");
        }
        if (returnDate == null) {
            throw new IllegalArgumentException("returnDate must not be null");
        }

        Loan loan = loans.stream()
                .filter(l -> l.getId().equals(loanId))
                .findFirst()
                .orElseThrow(() -> ItemNotFoundException.forLoanId(loanId));

        if (returnDate.isBefore(loan.getBorrowedOn())) {
            throw new IllegalArgumentException(
                    "returnDate " + returnDate + " must not be before borrowedOn " + loan.getBorrowedOn());
        }

        loan.markReturned(returnDate);

        long daysLate = Math.max(0, ChronoUnit.DAYS.between(loan.getDueOn(), returnDate));
        LibraryItem item = findItem(loan.getBarcode());
        BigDecimal fine = item.finePerDay()
                .multiply(BigDecimal.valueOf(daysLate))
                .setScale(2, RoundingMode.HALF_UP);

        if (fine.signum() > 0) {
            Member member = findMember(loan.getMemberId());
            member.addFine(fine);
        }

        item.setStatus(ItemStatus.ON_SHELF);
        return fine;
    }

    public void payFine(String memberId, BigDecimal amount) {
        requireNonBlank(memberId, "memberId");
        Member member = findMember(memberId);
        member.pay(amount);
    }

    public List<LibraryItem> availableCopies(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("isbn must not be blank");
        }
        return items.values().stream()
                .filter(item -> item.getIsbn().equals(isbn))
                .filter(item -> item.getStatus() == ItemStatus.ON_SHELF)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<LibraryItem> searchByAuthor(String author) {
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("author must not be blank");
        }
        String needle = author.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getAuthor().toLowerCase().contains(needle))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<Loan> overdueLoans() {
        LocalDate today = LocalDate.now();
        return loans.stream()
                .filter(Loan::isActive)
                .filter(loan -> loan.getDueOn().isBefore(today))
                .sorted(Comparator.comparingLong(
                        (Loan loan) -> ChronoUnit.DAYS.between(loan.getDueOn(), today)).reversed())
                .collect(Collectors.toUnmodifiableList());
    }

    public Map<String, List<Loan>> loansByMember() {
        Map<String, List<Loan>> grouped = loans.stream()
                .collect(Collectors.groupingBy(Loan::getMemberId, LinkedHashMap::new, Collectors.toList()));
        Map<String, List<Loan>> immutable = new LinkedHashMap<>();
        for (Map.Entry<String, List<Loan>> entry : grouped.entrySet()) {
            immutable.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(immutable);
    }

    private LibraryItem findItem(String barcode) {
        LibraryItem item = items.get(barcode);
        if (item == null) {
            throw ItemNotFoundException.forBarcode(barcode);
        }
        return item;
    }

    private Member findMember(String memberId) {
        Member member = members.get(memberId);
        if (member == null) {
            throw ItemNotFoundException.forMemberId(memberId);
        }
        return member;
    }
    private String nextLoanId() {
        return String.format("LN-%04d", loanSequence.getAndIncrement());
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
