package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

// Stmt -> 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
public class IfStmt extends StmtNode{
    public ExprNode condition; // Cond -> LOrExp，直接用 ExprNode 即可
    public StmtNode thenStmt;
    public StmtNode elseStmt;  // 可能为null

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
