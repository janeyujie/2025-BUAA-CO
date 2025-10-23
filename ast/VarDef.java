package ast;

import java.util.List;

// VarDef -> Ident [ '[' ConstExp ']' ] [ '=' Initval ]
public class VarDef extends Node {
    public String identName;
    public ExprNode arrayIndex;
    public InitVal initialValue;
}
