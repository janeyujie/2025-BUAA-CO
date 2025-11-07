package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

// Stmt -> [Exp] ';'
public class ExprStmt extends StmtNode {
    public ExprNode expr; // 可能为 null

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
