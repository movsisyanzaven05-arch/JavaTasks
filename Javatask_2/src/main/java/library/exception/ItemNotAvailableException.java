package library.exception;

public class ItemNotAvailableException extends Exception {

    public ItemNotAvailableException(String barcode) {
        super("item with barcode '" + barcode + "' is not available for borrowing");
    }
}
