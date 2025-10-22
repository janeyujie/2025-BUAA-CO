package ast;

import java.util.List;

//  VarDecl -> BType VarDef { ',' VarDef } ';'
public class VarDecl extends DeclNode {
    public Boolean isStatic;
    public List<VarDef> varDefs;
}
