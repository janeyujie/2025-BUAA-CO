package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

public class ContinueStmt extends StmtNode {
    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
