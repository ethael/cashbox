package protect.cashbox.util;

public class ParseException extends Exception {
    public ParseException(String message, Exception rootCause) {
        super(message, rootCause);
    }
}
