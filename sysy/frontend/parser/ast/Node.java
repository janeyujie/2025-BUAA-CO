package sysy.frontend.parser.ast;

import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

public abstract class Node {
    public int lineNumber;
    /*private final List<Node> children= new ArrayList<>();

    public void addChild(Node child) {
        if (child != null) {
            this.children.add(child);
        }
    }

    public List<Node> getChildren() {
        return this.children;
    }

    public void print(String indent) {
        System.out.println(indent + this.getClass().getSimpleName());
        for (Node child : this.children) {
            child.print(indent + "  ");
        }
    }*/

    //public abstract void formatOutput() throws IOException;
    public abstract VisitResult accept(Visitor visitor);
}

