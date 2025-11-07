package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

// Stmt -> LVal '=' Exp ';'
public class AssignStmt extends StmtNode{
    public LVal lVal;
    public ExprNode rValue;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
