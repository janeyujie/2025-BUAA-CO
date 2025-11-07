package sysy.frontend.visitor;

import sysy.Exception.SemanticException;
import sysy.error.SemanticError;
import sysy.frontend.parser.ast.*;
import sysy.frontend.parser.ast.Number;
import sysy.frontend.symtable.*;
import sysy.frontend.symtable.symbol.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SemanticAnalyzer implements Visitor {

    private SymbolTable currentTable;
    private final List<SemanticError> errors = new ArrayList<>();

    // 检查函数的return
    private FuncSymbol currentFunc = null;

    // 检查break/continue是否在循环内
    private int loopDepth = 0;

    // 保存当前上下文，判断变量是否为static
    private boolean isVarDefStatic = false;

    public SemanticAnalyzer() {
        this.currentTable = new SymbolTable();
    }

    public List<SemanticError> getErrors() {
        Collections.sort(errors);
        return errors;
    }

    // TODO:get output
    public String getSymbolTable() {
        // 向上找到最上层的符号表
        SymbolTable root = currentTable;
        while (root.preTable != null) {
            root = root.preTable;
        }
        List<String> lines = new ArrayList<>();
        root.dumpTable(lines);
        String output = "";
        for (String line : lines) {
            output += line + "\n";
        }
        return output;
    }

    // 程序的入口
    public void visit(Node root) {
        root.accept(this);
    }

    public VisitResult visit(CompUnit node) {
        for (Node topLevelNode : node.topLevelNodes) {
            topLevelNode.accept(this);
        }
        return null;
    }

    public VisitResult visit(FuncDef node) {
        // 检查函数是否重复定义
        FuncSymbol funcSymbol = new FuncSymbol();
        if (currentTable.contains(node.funcName)) {
            errors.add(new SemanticError(node.lineNumber, "b"));
            return null;
        } else if (Objects.equals(node.funcName, "main")) {
            // main函数不进入符号表中
            funcSymbol.returnType = node.returnType;
            funcSymbol.ident = node.funcName;
            funcSymbol.table = currentTable;
            currentFunc = funcSymbol;
        } else{
            funcSymbol.returnType = node.returnType;
            funcSymbol.ident = node.funcName;
            funcSymbol.table = currentTable;
            currentTable.insertSymbol(funcSymbol);
            currentFunc = funcSymbol;
        }
        // 进入函数体
        currentTable = currentTable.createSubTable();

        if (node.params != null) {
            for (FuncFParam param : node.params) {
                VarSymbol sym = new VarSymbol();
                sym.ident = param.paramName;
                // 同一级也可能出现重复定义
                if (currentTable.contains(param.paramName)) {
                    errors.add(new SemanticError(param.lineNumber, "b"));
                }
                sym.isArray = param.isArray;
                sym.table = currentTable;
                currentTable.insertSymbol(sym);
                funcSymbol.params.add(sym);
            }
        }

        // 调用block的visit方法
        node.funcBody.accept(this);

        // 检查返回类型
        if (Objects.equals(funcSymbol.returnType, "int")) {
            Block body = node.funcBody;
            Node lastItem = null;

            if(body.items != null && !body.items.isEmpty()) {
                lastItem = body.items.get(body.items.size() - 1);
            }
            // 判断最后一个 BlockItem 是否是 ReturnStmt
            // 如果函数体为空，或者最后一个语句不是 return，就报错
            if (lastItem == null || !(lastItem instanceof ReturnStmt)) {
                errors.add(new SemanticError(node.funcBody.lineNumber, "g"));
            }
        }

        // 退出函数
        currentTable = currentTable.preTable;
        currentFunc = null;
        return null;
    }

    @Override
    public VisitResult visit(ConstDecl node) {
        for(ConstDef def: node.constDefs) {
            def.accept(this);
        }
        return null;
    }

    @Override
    public VisitResult visit(VarDecl node) {
        this.isVarDefStatic = node.isStatic;
        for(VarDef def: node.varDefs) {
            def.accept(this);
        }
        this.isVarDefStatic = false;
        return null;
    }

    // 暂时不管变量的初始值
    public VisitResult visit(VarDef node) {
        // 检查变量是否重复定义
        if (currentTable.contains(node.identName)) {
            errors.add(new SemanticError(node.lineNumber, "b"));
        }
        VarSymbol sym = new VarSymbol();
        sym.ident = node.identName;
        if (this.isVarDefStatic)
            sym.btype = 2;
        else
            sym.btype = 0;

        if (node.arrayDim != null) {
            sym.isArray = true;
            sym.dim = node.arrayDim.accept(this).number;
        } else {
            sym.isArray = false;
        }

        sym.table = currentTable;
        currentTable.insertSymbol(sym);
        return null;
    }

    @Override
    public VisitResult visit(ConstDef node) {

        // 检查变量是否重复定义
        if (currentTable.contains(node.identName)) {
            errors.add(new SemanticError(node.lineNumber, "b"));
        }
        VarSymbol sym = new VarSymbol();
        sym.ident = node.identName;
        sym.btype = 1;
        sym.isArray = node.arrayDim != null;

        sym.table = currentTable;
        currentTable.insertSymbol(sym);
        return null;
    }

    @Override
    public VisitResult visit(InitVal node) {
        VisitResult res = new VisitResult();
        if (node.isArray) {
            for (ExprNode expr: node.arrayInits) {
                expr.accept(this);
            }
        } else {
            res.number = node.singleInit.accept(this).number;
        }
        return res;
    }

    @Override
    public VisitResult visit(Block node) {
        for(Node item: node.items) {
            if (item instanceof Block) {
                currentTable = currentTable.createSubTable();
                item.accept(this);
                currentTable = currentTable.preTable;
            } else {
                item.accept(this);
            }
        }
        return null;
    }


    @Override
    public VisitResult visit(IfStmt node) {
        // 检查Cond
        if (node.condition != null) {
            node.condition.accept(this);
        }

        if (node.thenStmt instanceof Block) {
            currentTable = currentTable.createSubTable();
            node.thenStmt.accept(this);
            currentTable = currentTable.preTable;
        } else {
            node.thenStmt.accept(this);
        }

        if (node.elseStmt != null) {
            if (node.elseStmt instanceof Block) {
                currentTable = currentTable.createSubTable();
                node.elseStmt.accept(this);
                currentTable = currentTable.preTable;
            } else {
                node.elseStmt.accept(this);
            }
        }
        return null;
    }

    @Override
    public VisitResult visit(ForStmt node) {
        loopDepth++;

        if (node.initStmts != null) {
            for (AssignStmt init : node.initStmts) {
                init.accept(this);
            }
        }

        if (node.condition != null) {
            node.condition.accept(this);
        }

        if (node.updateStmts != null) {
            for (AssignStmt update : node.updateStmts) {
                update.accept(this);
            }
        }

        if (node.body != null) {
            if (node.body instanceof Block) {
                currentTable = currentTable.createSubTable();
                node.body.accept(this);
                currentTable = currentTable.preTable;
            } else
                node.body.accept(this);
        }

        loopDepth--;
        return null;
    }

    @Override
    public VisitResult visit(PrintfStmt node) {
        String formatString = node.formatString;
        int formatSpecifier = 0;

        for (int i = 0; i < formatString.length(); i++) {
            if (formatString.charAt(i) == '%' && formatString.charAt(i + 1) == 'd') {
                formatSpecifier++;
            }
        }
        int argSize = node.args.size();
        if (formatSpecifier != argSize) {
            // printf中格式字符与表达式个数不匹配
            errors.add(new SemanticError(node.lineNumber, "l"));
        }
        // 还需要把这里的Exp遍历一遍！！
        for (ExprNode expr: node.args) {
            expr.accept(this);
        }

        return null;
    }

    @Override
    public VisitResult visit(ReturnStmt node) {
        if (currentFunc == null) {
            System.out.println("return 不在函数内");
            return null;
        }
        // 检查返回值是否匹配
        if (Objects.equals(currentFunc.returnType, "void")) {
            if (node.returnValue != null) {
                errors.add(new SemanticError(node.lineNumber, "f"));
            }
//        } else {
//            // 有返回值的函数缺少return
//            if (node.returnValue == null) {
//                errors.add(new SemanticError(node.lineNumber, "g"));
//            } else {
//                VisitResult res = node.returnValue.accept(this);
//                if (res.ifArray) {
//                    errors.add(new SemanticError(node.lineNumber, "f"));
//                }
//            }
        }
        if (node.returnValue != null) {
            node.returnValue.accept(this);
        }
        return null;
    }

    @Override
    public VisitResult visit(BreakStmt node) {
        // 检查 m 类错误: 在非循环块中使用 break
        if (loopDepth == 0) {
            errors.add(new SemanticError(node.lineNumber, "m"));
        }
        return null;
    }

    @Override
    public VisitResult visit(ContinueStmt node) {
        // 检查 m 类错误: 在非循环块中使用 continue
        if (loopDepth == 0) {
            errors.add(new SemanticError(node.lineNumber, "m"));
        }
        return null;
    }

    @Override
    public VisitResult visit(AssignStmt node) {
        VisitResult res1 = node.lVal.accept(this);
        VisitResult res2 = node.rValue.accept(this);

        if (res1 != null  && res1.ifConst) {
            // 不能改变常量的值
            errors.add(new SemanticError(node.lVal.lineNumber, "h"));
        }

        return null;
    }

    @Override
    public VisitResult visit(ExprStmt node) {
        VisitResult res = null;
        if (node.expr != null) {
            res = node.expr.accept(this);
        }
        return res;
    }


    @Override
    public VisitResult visit(LVal node) {
        VisitResult res = new VisitResult();
        Symbol sym = currentTable.getSymbol(node.identName);
        if (sym == null || sym instanceof FuncSymbol) {
            // 使用未定义的变量
            errors.add(new SemanticError(node.lineNumber, "c"));
            return null;
        }
        // 数组的情况
        // a[]是数组，a[1]不是
        if (((VarSymbol) sym).isArray && node.arrayIndex == null) {
            res.ifArray = true;
        } else {
            res.ifArray = false;
        }
        if (node.arrayIndex != null) {
            node.arrayIndex.accept(this);
        }
        if (((VarSymbol) sym).btype == 1) {
            res.ifConst = true;
        }
        return res;
    }

    @Override
    public VisitResult visit(BinaryOpExp node) {
        VisitResult res1 = node.left.accept(this);
        VisitResult res2 = node.right.accept(this);

        // 不可能出现表达式左右类型不匹配的情况
        return res1;
    }

    @Override
    public VisitResult visit(UnaryExp node) {
        VisitResult res = node.operand.accept(this);
        return res;
    }

    @Override
    public VisitResult visit(FuncCallExp node) {
        VisitResult res = new VisitResult();
        if (Objects.equals(node.funcName, "getint")) {
            // getint参数不匹配
            if (!node.args.isEmpty()) {
                errors.add(new SemanticError(node.lineNumber, "d"));
            }
            return null;
        }
        Symbol sym = currentTable.getSymbol(node.funcName);
        // 函数未定义
        if (!(sym instanceof FuncSymbol)) {
            errors.add(new SemanticError(node.lineNumber, "c"));
            return null;
        }
        if (Objects.equals(((FuncSymbol) sym).returnType, "int")) {
            res.ifArray = false;
        }
        List<VarSymbol> params = ((FuncSymbol) sym).params;

        // 检查参数个数
        if (node.args.size() != params.size()) {
            errors.add(new SemanticError(node.lineNumber, "d"));
            return null;
        }

        // 检查参数类型
        for (int i = 0; i < node.args.size(); i++) {
            ExprNode arg = node.args.get(i);
            VisitResult res1 = arg.accept(this);

            if (res1 != null && res1.ifArray != params.get(i).isArray) {
                // 参数类型不匹配
                errors.add(new SemanticError(node.lineNumber, "e"));
                //return null;
            }
        }

        return res;
    }

    @Override
    public VisitResult visit(Number number) {
        VisitResult res = new VisitResult();
        res.ifArray = false;
        res.number = number.value;
        return res;
    }

}
