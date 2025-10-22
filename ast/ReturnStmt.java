package ast;

// Stmt -> 'return' [Exp] ';'
public class ReturnStmt extends StmtNode {
    public ExprNode returnValue;
}
