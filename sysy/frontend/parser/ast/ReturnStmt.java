package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

// Stmt -> 'return' [Exp] ';'
public class ReturnStmt extends StmtNode {
    public ExprNode returnValue;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
