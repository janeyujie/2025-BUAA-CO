package ast;

import java.util.List;

// Block -> '{' { BlockItem } '}'
public class Block extends StmtNode {
    public List<Node> items; // 可以是 DeclNode 或 StmtNode
}
