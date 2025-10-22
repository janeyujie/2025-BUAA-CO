package ast;

public class BinaryOpExp extends ExprNode {
    public Operator op; // +, -, *, /, &&, ||, ==, !=, <, <=, >, >=
    public ExprNode left;
    public ExprNode right;
}
