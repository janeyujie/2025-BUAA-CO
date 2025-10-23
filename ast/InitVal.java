package ast;

import java.util.List;

// InitVal -> Exp | '{' [ Exp { ',' Exp } ] '}'
public class InitVal extends Node{
    public boolean isArray;
    public ExprNode singleInit;
    public List<ExprNode> arrayInits;
}
