package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

import java.util.List;

// InitVal -> Exp | '{' [ Exp { ',' Exp } ] '}'
public class InitVal extends Node{
    public boolean isArray;
    public ExprNode singleInit;
    public List<ExprNode> arrayInits;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
