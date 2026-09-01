package library.exception;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(String id) {
        super("no record found for id '" + id + "'");
    }

    public static ItemNotFoundException forBarcode(String barcode) {
        return new ItemNotFoundException(barcode);
    }

    public static ItemNotFoundException forMemberId(String memberId) {
        return new ItemNotFoundException(memberId);
    }

    public static ItemNotFoundException forLoanId(String loanId) {
        return new ItemNotFoundException(loanId);
    }
}
