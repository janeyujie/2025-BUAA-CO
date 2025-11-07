package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

// FuncFParam -> BType Ident ['[' ']']
public class FuncFParam extends Node{
    public String paramName;
    public boolean isArray;

    @Override
    public VisitResult accept(Visitor visitor) {
        return null;
    }
}
