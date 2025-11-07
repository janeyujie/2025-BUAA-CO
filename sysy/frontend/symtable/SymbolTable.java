package sysy.frontend.symtable;

import sysy.frontend.symtable.symbol.Symbol;

import java.util.*;

public class SymbolTable {
    // 指向父作用域
    public SymbolTable preTable = null;
    // 指向其所有子作用域
    public final List<SymbolTable> nextTables = new ArrayList<SymbolTable>();
    // 当前作用域的符号表
    public final Map<String, Symbol> symbolMap = new HashMap<>();
    // 用于输出符号表信息
    private static int nextScope = 1;
    public int scope;
    private final List<Symbol> declaredSymbols = new ArrayList<>();

    public SymbolTable() {
        this.scope = nextScope++;
    }

    // 检查符号是否在当前作用域中定义
    public boolean contains(String name) {
        return symbolMap.containsKey(name);
    }

    // 在当前作用域中插入一个符号
    public void insertSymbol(Symbol symbol) {
        symbolMap.put(symbol.ident, symbol);
        symbol.table = this;
        declaredSymbols.add(symbol);
    }

    // 从当前作用域开始，递归向上查找符号
    public Symbol getSymbol(String ident) {
        if (symbolMap.containsKey(ident)) {
            return symbolMap.get(ident);
        }

        if (preTable != null) {
            return preTable.getSymbol(ident);
        } else {
            return null;
        }
    }

    // 创建并连接一个新的 子符号表
    public SymbolTable createSubTable() {
        SymbolTable subTable = new SymbolTable();
        nextTables.add(subTable);
        subTable.preTable = this;
        return subTable;
    }

    // 递归输出
    public void dumpTable(List<String> output) {

        for (Symbol s: declaredSymbols){
            if (s.getSymbolTableOutput() != null)  {
                output.add(s.getSymbolTableOutput());
            }
        }
        for (SymbolTable sub: nextTables) {
            sub.dumpTable(output);
        }
    }
}
