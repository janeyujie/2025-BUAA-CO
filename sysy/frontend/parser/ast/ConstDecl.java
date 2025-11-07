package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

import java.util.List;

// ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
public class ConstDecl extends DeclNode {
    public List<ConstDef> constDefs;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
