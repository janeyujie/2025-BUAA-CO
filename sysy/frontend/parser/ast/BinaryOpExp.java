package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

public class BinaryOpExp extends ExprNode {
    public Operator op; // +, -, *, /, &&, ||, ==, !=, <, <=, >, >=
    public ExprNode left;
    public ExprNode right;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
