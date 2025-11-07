package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

// Stmt -> 'break' ';'
public class BreakStmt extends StmtNode {
    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
