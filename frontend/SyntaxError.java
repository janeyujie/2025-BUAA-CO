package frontend;

public class SyntaxError implements Comparable<SyntaxError> {
    private final int lineNo;
    private final String errorCode;

    public SyntaxError(int lineNo, String errorCode) {
        this.lineNo = lineNo;
        this.errorCode = errorCode;
    }

    @Override
    public int compareTo(SyntaxError o) {
        return this.lineNo - o.lineNo;
    }

    public String toString() {
        return lineNo + " " + errorCode;
    }
}
