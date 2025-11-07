package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

// Number -> IntConst
public class Number extends ExprNode {
    public int value;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
