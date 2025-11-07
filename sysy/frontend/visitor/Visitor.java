package sysy.frontend.visitor;

import sysy.error.SemanticError;
import sysy.frontend.parser.ast.*;
import sysy.frontend.parser.ast.Number;
import sysy.frontend.symtable.SymbolTable;
import sysy.frontend.symtable.symbol.FuncSymbol;
import sysy.frontend.symtable.symbol.Symbol;
import sysy.frontend.symtable.symbol.VarSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface Visitor {

    VisitResult visit(CompUnit node);
    VisitResult visit(FuncDef node);
    VisitResult visit(ConstDecl node);
    VisitResult visit(VarDecl node);
    VisitResult visit(VarDef varDef);
    VisitResult visit(ConstDef node);
    VisitResult visit(InitVal node);
    VisitResult visit(Block node);
    VisitResult visit(AssignStmt node);
    VisitResult visit(IfStmt node);
    VisitResult visit(ForStmt node);
    VisitResult visit(PrintfStmt node);
    VisitResult visit(ReturnStmt node);
    VisitResult visit(BreakStmt node);
    VisitResult visit(ContinueStmt node);
    VisitResult visit(ExprStmt node);
    VisitResult visit(LVal node);
    VisitResult visit(BinaryOpExp node);
    VisitResult visit(UnaryExp node);
    VisitResult visit(FuncCallExp node);
    VisitResult visit(Number number);
}
