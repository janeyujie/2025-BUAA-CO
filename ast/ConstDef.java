package ast;

import java.util.List;

// ConstDef -> Ident [ '[' ConstExp ']' ] '=' ConstInitval
public class ConstDef extends Node{
    public String identName;
    public List<ExprNode> arrayIndexes;
    public InitVal initialValue;
}
