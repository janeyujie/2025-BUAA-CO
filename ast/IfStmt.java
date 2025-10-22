package ast;

// Stmt -> 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
public class IfStmt extends StmtNode{
    public ExprNode condition; // Cond -> LOrExp，直接用 ExprNode 即可
    public StmtNode thenStmt;
    public StmtNode elseStmt;  // 可能为null
}
