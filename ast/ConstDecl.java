package ast;

import java.util.List;

// ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
public class ConstDecl extends DeclNode {
    public List<ConstDef> constDefs;
}
