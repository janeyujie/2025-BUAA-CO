package ast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Node {
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
}

