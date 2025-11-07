package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

import java.util.List;

// 'printf' '(' StringConst {',' Exp} ')' ';'
public class PrintfStmt extends StmtNode {
    public String formatString;
    public List<ExprNode> args;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
