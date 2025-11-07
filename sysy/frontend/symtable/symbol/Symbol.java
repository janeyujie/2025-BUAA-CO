package sysy.frontend.symtable.symbol;

import sysy.frontend.symtable.SymbolTable;

import java.util.List;
import java.util.Objects;

// 应该还是需要把func和var分开
public abstract class Symbol {
    // 标识符名
    public String ident;
    // 符号属于哪个符号表
    public SymbolTable table;

    public String getSymbolTableOutput() {
        if (this.ident.equals("main")) return null;
        String type = "";
        if (this instanceof VarSymbol) {
            int btype = ((VarSymbol) this).btype;
            if (((VarSymbol) this).isArray) {
                if (btype== 0) type = "IntArray";
                else if (btype == 1) type = "ConstIntArray";
                else if (btype == 2) type = "StaticIntArray";
            } else {
                if (btype == 0) type = "Int";
                else if (btype == 1) type = "ConstInt";
                else if (btype == 2) type = "StaticInt";
            }
        } else if (this instanceof FuncSymbol) {
            if (Objects.equals(((FuncSymbol) this).returnType, "int")) {
                type = "IntFunc";
            } else if (Objects.equals(((FuncSymbol) this).returnType, "void")) {
                type = "VoidFunc";
            }
        }
        return table.scope + " " + ident + " " + type;
    }
}
