package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

// 表达式节点
public abstract class ExprNode extends Node {

    @Override
    public abstract VisitResult accept(Visitor visitor);
}
