package sysy.error;

public class SyntaxError extends Error {

    public SyntaxError(int lineNo, String errorCode) {
        super(lineNo, errorCode);
    }
}
