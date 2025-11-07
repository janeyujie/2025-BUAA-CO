package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

import java.util.List;

// CompUnit -> {Decl} {FuncDef} MainFuncDef
public class CompUnit extends Node{
    // 可以是VarDecl, ConstDecl, FuncDef
    public List<Node> topLevelNodes;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
