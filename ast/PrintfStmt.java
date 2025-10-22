package ast;

import java.util.List;

// 'printf' '(' StringConst {',' Exp} ')' ';'
public class PrintfStmt extends StmtNode {
    public String formatString;
    public List<ExprNode> args;
}
