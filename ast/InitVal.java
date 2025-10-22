package ast;

import java.util.List;

// InitVal -> Exp | '{' [ Exp { ',' Exp } ] '}'
public class InitVal extends Node{
    public boolean isArray;
    public ExprNode singleExpr;
    public List<ExprNode> arrayExprs;
}
