package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

// UnaryExp -> UnaryOp UnaryExp
public class UnaryExp extends ExprNode {
    public Operator op; // +, -, !
    public ExprNode operand;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
