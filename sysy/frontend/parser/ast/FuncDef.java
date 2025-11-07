package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

import java.util.List;

// FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
public class FuncDef extends Node{
    public String returnType;
    public String funcName;
    public List<FuncFParam> params;
    public Block funcBody;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
