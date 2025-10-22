package ast;

import java.util.List;

// 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
// ForStmt -> LVal '=' Exp { ',' LVal '=' Exp }
public class ForStmt extends StmtNode {
    public List<AssignStmt> initStmts;  // 前一个forstmt
    public ExprNode condition; // cond
    public List<AssignStmt> updateStmts; // 后一个forstmt
    public StmtNode body;

}
