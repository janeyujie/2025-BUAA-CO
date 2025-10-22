package ast;

// Stmt -> LVal '=' Exp ';'
public class AssignStmt extends StmtNode{
    public LVal lVal;
    public ExprNode rValue;
}
