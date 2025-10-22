package ast;

// UnaryExp -> UnaryOp UnaryExp
public class UnaryExp extends ExprNode {
    public Operator op; // +, -, !
    public ExprNode operand;
}
