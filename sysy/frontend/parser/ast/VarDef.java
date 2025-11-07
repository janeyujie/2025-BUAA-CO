package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

// VarDef -> Ident [ '[' ConstExp ']' ] [ '=' Initval ]
public class VarDef extends Node {
    public String identName;
    public ExprNode arrayDim;
    public InitVal initialValue;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
