package ast;

// 'repeat' Stmt 'until' '(' Cond ')' ';'
public class RepeatStmt extends StmtNode {
    public StmtNode thenStmt;
    public ExprNode condition;
}
