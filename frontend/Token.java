package frontend;

public class Token {
    private TokenType type;
    private String content;
    private int lineno;

    public Token(TokenType type, String content, int lineno) {
        this.type = type;
        this.content = content;
        this.lineno = lineno;
    }
    public Token(){}

    public void setType(TokenType type) {
        this.type = type;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setLineno(int lineno) {
        this.lineno = lineno;
    }
    public TokenType getType() {
        return type;
    }
    public String getContent() {
        return content;
    }
    public int getLineno() {
        return lineno;
    }

    public String toString() {
        return type.name() + " " + content + "\n";
    }

    public String toError() {
        return lineno + " " + content + "\n";
    }
}
