package ast;

import java.util.List;

// CompUnit -> {Decl} {FuncDef} MainFuncDef
public class CompUnit extends Node{
    // 可以是VarDecl, ConstDecl, FuncDef
    public List<Node> topLevelNodes;
}
