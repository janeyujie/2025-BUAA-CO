package ast;

import java.util.List;

// 函数调用
// UnaryExp -> Ident '(' [FuncRParams] ')'
public class FunctionCall extends ExprNode {
    public String funcName;
    public List<ExprNode> args;
}
