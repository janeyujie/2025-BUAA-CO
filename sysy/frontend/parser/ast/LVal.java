package sysy.frontend.parser.ast;

import sysy.frontend.symtable.symbol.Symbol;
import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;

// LVal -> Ident ['[' Exp ']']
public class LVal extends ExprNode {
    public String identName;
    public ExprNode arrayIndex;
    // 记录下在语义分析时，这个标识符对应的符号，避免出现作用域中还有一个同名变量出现的调用顺序混乱的情况
    public Symbol symbol;

    @Override
    public VisitResult accept(Visitor visitor) {
        return visitor.visit(this);
    }
}
