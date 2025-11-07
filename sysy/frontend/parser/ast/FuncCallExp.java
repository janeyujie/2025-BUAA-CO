package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

import java.util.List;

// 函数调用
// UnaryExp -> Ident '(' [FuncRParams] ')'
public class FuncCallExp extends ExprNode {
    public String funcName;
    public List<ExprNode> args;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
