package sysy.error;

public class SemanticError extends Error {

    public SemanticError(int lineNo, String errorCode) {
        super(lineNo, errorCode);

    }

}
