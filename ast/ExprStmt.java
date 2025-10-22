package ast;

// Stmt -> [Exp] ';'
public class ExprStmt extends StmtNode {
    public ExprNode expr; // 可能为 null
}
