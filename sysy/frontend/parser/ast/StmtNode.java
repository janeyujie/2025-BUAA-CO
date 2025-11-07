package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

// 语句节点
public abstract class StmtNode extends Node {
    public abstract VisitResult accept(Visitor visitor);
}
