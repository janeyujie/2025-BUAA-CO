package sysy.frontend.parser.ast;

public enum Operator {
    // Binary Operators
    ADD("+"), SUB("-"), MUL("*"), DIV("/"), MOD("%"),
    EQ("=="), NE("!="), LT("<"), GT(">"), LE("<="), GE(">="),
    AND("&&"), OR("||"),
    // Unary Operators
    POS("+"), NEG("-"), NOT("!"); // POS for unary +, NEG for unary -

    private String value;
    Operator(String value) {
        this.value = value;
    }
    public String toString() {
        return value;
    }
}
