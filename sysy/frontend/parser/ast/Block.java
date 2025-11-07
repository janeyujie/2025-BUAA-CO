package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

import java.util.List;

// Block -> '{' { BlockItem } '}'
public class Block extends StmtNode {
    public List<Node> items; // 可以是 DeclNode 或 StmtNode

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
