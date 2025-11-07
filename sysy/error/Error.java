package sysy.error;

public class Error implements Comparable<Error> {
    private final int lineNo;
    private final String errorCode;

    public Error(int lineNo, String errorCode) {
        this.lineNo = lineNo;
        this.errorCode = errorCode;
    }

    @Override
    public int compareTo(Error o) {
        if (this.lineNo != o.lineNo) {
            return this.lineNo - o.lineNo;
        }
        return this.errorCode.compareTo(o.errorCode);
    }

    public String toString() {
        return lineNo + " " + errorCode;
    }
}
