package library.exception;

public class LoanLimitExceededException extends Exception {

    public LoanLimitExceededException(String memberId, int limit) {
        super("member '" + memberId + "' has reached the loan limit of " + limit);
    }
}
