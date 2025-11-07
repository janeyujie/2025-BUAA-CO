package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

import java.util.List;

//  VarDecl -> BType VarDef { ',' VarDef } ';'
public class VarDecl extends DeclNode {
    public Boolean isStatic = false;
    public List<VarDef> varDefs;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
