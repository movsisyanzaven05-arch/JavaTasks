package library.exception;

import java.math.BigDecimal;


public class OutstandingFineException extends Exception {

    public OutstandingFineException(String memberId, BigDecimal balance) {
        super("member '" + memberId + "' has an outstanding balance of " + balance
                + " and cannot borrow until it is paid down");
    }
}
