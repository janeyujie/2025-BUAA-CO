package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

// ConstDef -> Ident [ '[' ConstExp ']' ] '=' ConstInitval
public class ConstDef extends Node{
    public String identName;
    public ExprNode arrayDim;
    public InitVal initialValue;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
