package ast;

import java.util.List;

// LVal -> Ident ['[' Exp ']']
public class LVal extends ExprNode {
    public String identName;
    public ExprNode arrayIndex;
}
