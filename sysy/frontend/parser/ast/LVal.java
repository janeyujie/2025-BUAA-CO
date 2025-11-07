package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

// LVal -> Ident ['[' Exp ']']
public class LVal extends ExprNode {
    public String identName;
    public ExprNode arrayIndex;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
