package ast;

import java.util.List;

// FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
public class FuncDef extends Node{
    public String returnType;
    public String funcName;
    public List<FuncFParam> params;
    public Block funcBody;
}
