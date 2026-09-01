# LIB-101 — City Library lending service (console)

Plain Java 17 console app for borrowing, returning, fines, and search. No frameworks,
no database — just the JDK and JUnit 5 for tests.

## Project layout

```
src/main/java/
  Main.java                          demo that walks through every acceptance criterion
  library/model/
    ItemStatus.java, Membership.java
    LibraryItem.java (abstract), PaperBook.java, AudioBook.java, Magazine.java
    Member.java, Loan.java
  library/exception/
    ItemNotAvailableException.java   (checked)
    LoanLimitExceededException.java  (checked)
    OutstandingFineException.java    (checked)
    ItemNotFoundException.java       (unchecked)
  library/service/
    LibraryService.java

src/test/java/library/service/
  LibraryServiceTest.java            10 JUnit 5 tests, one or more per AC
```

## Build & run

This is a standard Maven project.

```bash
mvn test          # run the JUnit 5 suite
mvn compile exec:java -Dexec.mainClass=Main   # run the console demo
```

Or without Maven, using just the JDK (17+):

```bash
javac -d out $(find src/main -name "*.java")
java -cp out Main
```

> Note: building this in a network-sandboxed environment without access to Maven
> Central will fail to resolve the `junit-jupiter` test dependency. The main
> sources have no external dependencies and compile/run with plain `javac`/`java`
> as shown above; `mvn test` needs normal internet access to Maven Central.

## Design notes (mapping back to the ticket)

- **No `switch`/`instanceof` over item type.** `loanDays()` and `finePerDay()`
  are abstract on `LibraryItem` and overridden by `PaperBook` (21d / 0.20),
  `AudioBook` (14d / 0.30), and `Magazine` (7d / 0.50).
- **No `if (membership == BASIC)` in the service.** `Membership` carries
  `maxLoans` as an enum field; `LibraryService.borrow` reads
  `member.getMembership().getMaxLoans()`.
- **Money** is `BigDecimal`, scale 2, `HALF_UP`, everywhere — fines, balances,
  payments. No `double` appears anywhere in the model or service.
- **Barcode vs ISBN.** `barcode` is the unique key for a physical copy and is
  used for `equals`/`hashCode` on `LibraryItem`; `isbn` identifies the title
  and is what `availableCopies(isbn)` groups by. They're never used
  interchangeably.
- **Encapsulation / AC10.** `availableCopies`, `searchByAuthor`, `overdueLoans`,
  and `loansByMember` all return defensive, unmodifiable copies
  (`Collectors.toUnmodifiableList()` / `List.copyOf()` / `Map.copyOf()`), so
  callers can't mutate the service's internal state through them.
- **Exceptions.** The three business-rule exceptions
  (`ItemNotAvailableException`, `LoanLimitExceededException`,
  `OutstandingFineException`) are checked, since they represent expected
  outcomes a caller must handle. `ItemNotFoundException` is unchecked, since a
  bad barcode/member id/loan id is closer to a programming/data error; its
  message always includes the offending id.
- **`borrowOn(memberId, barcode, borrowedOn)`** is a small addition beyond the
  spec's `borrow(memberId, barcode)` — it lets `Main` and the AC9 test
  manufacture an already-overdue loan without waiting for real time to pass.
  `borrow(...)` simply calls it with `LocalDate.now()`.

## Definition of Done checklist

- [x] All acceptance criteria (AC1–AC10) demonstrated in `Main`
- [x] 10 JUnit 5 tests (≥8 required), one or more per AC
- [x] Packages: `model`, `service`, `exception`
- [x] `equals`/`hashCode` on `LibraryItem` (by barcode) and `Member` (by id)
- [x] No `double` for money, no public fields, no `catch (Exception e)`
- [x] Signatures use `List`/`Map`, not `ArrayList`/`HashMap`
- [ ] PR opened on `feature/LIB-101-lending`, reviewed by mentor — outside the
      scope of this generated code; push this project to that branch when ready.
