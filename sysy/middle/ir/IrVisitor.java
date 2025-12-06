package sysy.middle.ir;

import sysy.frontend.parser.ast.*;
import sysy.frontend.parser.ast.Number;
import sysy.frontend.symtable.SymbolTable;
import sysy.frontend.symtable.symbol.Symbol;
import sysy.frontend.symtable.symbol.VarSymbol;
import sysy.frontend.visitor.VisitResult;
import sysy.frontend.visitor.Visitor;
import sysy.middle.ir.instruction.*;
import sysy.middle.ir.type.IrType;
import sysy.middle.ir.value.IrConstant;
import sysy.middle.ir.value.IrGlobalVariable;
import sysy.middle.ir.value.IrParamValue;
import sysy.middle.ir.value.IrValue;

import java.util.*;

public class IrVisitor implements Visitor {

    // 最终生成的结果
    private IrModule module;
    // 当前所在的函数体
    private IrFunction currentFunction;
    // 当前所在的Block
    private IrBasicBlock currentBlock;
    // 当前的符号表
    private SymbolTable currentTable;
    // 将 SysY 的 符号(Symbol) 映射到 LLVM 的 值(IrValue)
    private Map<Symbol, IrValue> valueMap;

    private Stack<IrBasicBlock> loopContinueStack; // 'continue' 跳转的目标
    private Stack<IrBasicBlock> loopBreakStack;    // 'break' 跳转的目标

    private int globalVarCounter = 0;
    private int tempVarCounter = 0;
    private int labelCounter = 0;
    private int staticVarCounter = 0;

    // 生成唯一的全局变量名
    private String newGlobalName() {
        return "global_var_" + (globalVarCounter++);
    }

    // 创建一个唯一的临时寄存器名
    private String newTempReg() {
        return "%t" + tempVarCounter++;
    }

    // 创建一个唯一的标签名
    private String newLabel(String prefix) {
        return prefix + "_" + labelCounter++;
    }


    public IrVisitor(SymbolTable globalTable) {
        this.module = new IrModule();
        this.currentTable = globalTable;
        this.valueMap = new HashMap<>();
        this.loopContinueStack = new Stack<>();
        this.loopBreakStack = new Stack<>();
    }

    public IrModule getModule() {
        return module;
    }

    // 将指令添加到当前基本块
    private IrValue addInstr(IrInstruction instr) {
        this.currentBlock.addInstruction(instr);
        if (instr.irType != IrType.getVoid()) { // 如果指令产生值
            instr.name = newTempReg();
        }
        return instr;
    }

     //查找 LVal 对应的指针, 返回 IrValue 这个 LVal 在内存中的地址 %a.ptr
    private IrValue getLValPtr(LVal node) {
        // 直接使用语义分析中给lval绑定的符号
        Symbol sym = node.symbol;
        IrValue ptr = valueMap.get(sym);

        if (ptr == null) {
            throw new RuntimeException("变量 " + node.identName + " 在使用前未定义 (IrValue 为空)!");
        }

        if (node.arrayIndex != null) {
            // 数组——计算索引 i 并创建GEP指令
            IrValue index = node.arrayIndex.accept(this).value;
            IrInstruction gep = new IrGetElementPtr(ptr, index);
            return addInstr(gep);
        } else {
            // 函数参数——返回指针
            return ptr;
        }
    }

    // 将一个操作数i32转换为i1
    private IrValue convertOpToBool(IrValue operand) {
        if (operand.irType.equals(IrType.getInt32())) {
            return addInstr(new IrCompare("ne", operand, IrConstant.ZERO));
        }
        return operand;
    }

    @Override
    public VisitResult visit(CompUnit node) {
        for (Node topLevelNode : node.topLevelNodes) {
            topLevelNode.accept(this);
        }
        return null;
    }

    @Override
    public VisitResult visit(FuncDef node) {
        IrFunction func = module.createFunction(node.funcName, node.returnType);
        this.currentFunction = func;

        IrBasicBlock entryBlock = func.createBasicBlock(newLabel(""));
        this.currentBlock = entryBlock;

        // 找到当前函数对应的作用域
        this.currentTable = node.scope;

        // 处理函数参数
        if (node.params != null) {
            for (FuncFParam param : node.params) {
                // 获取参数名和类型
                Symbol paramSym = currentTable.getSymbol(param.paramName);
                IrType paramType = paramSym.getIrType();
                // 创建Ir参数值
                IrParamValue paramValue = new IrParamValue(paramType, "%" + param.paramName);
                func.addParam(paramValue);

                if(param.isArray) {
                    // 数组参数 (i32*), 直接将 %a 映射到符号 a
                    valueMap.put(paramSym, paramValue);
                } else {
                    // 普通参数 (i32), 分配内存 %a.ptr, 然后 store %a -> %a.ptr
                    IrAlloca ptr = new IrAlloca(paramValue.irType);
                    addInstr(ptr);
                    valueMap.put(paramSym, ptr);
                    addInstr(new IrStore(paramValue, ptr));
                }
            }
        }

        // 遍历函数体
        node.funcBody.accept(this);

        // 函数的终结
        if (!currentBlock.isTerminated()) {
            if (func.returnType.isVoid()) {
                addInstr(new IrReturn());
            } else {
                // 如果是int的话必须有返回值
                //addInstr(new IrReturn(IrConstant.ZERO));
                throw new RuntimeException("int 函数没有 return，这是已经处理过的i类错误");
            }
        }

        this.currentTable = this.currentTable.preTable;
        this.currentFunction = null;
        return null;
    }

    @Override
    public VisitResult visit(ConstDecl node) {
        for (ConstDef def : node.constDefs) {
            Symbol sym = currentTable.getSymbol(def.identName);
            IrType type = sym.getIrType();

            if (currentFunction == null) {
                // 全局变量的初始化应该是这样的@a = dso_local global [6 x i32] [i32 1, i32 2, i32 3, i32 4, i32 5, i32 6]
                IrGlobalVariable gv;

                // initValue不可能为空
                if (def.initialValue != null) {
                    if (def.initialValue.isArray) {
                        List<IrConstant> inits = new ArrayList<>();
                        for (Integer init : ((VarSymbol)sym).constArrayValues) {
                            inits.add(new IrConstant(init));
                        }
//                        for (ExprNode initExp : def.initialValue.arrayInits) {
//                            IrValue val = initExp.accept(this).value;
//                            if (!(val instanceof IrConstant)) {
//                                throw new RuntimeException("全局数组 " + def.identName + " 的初始值不是常量!");
//                            }
//                            inits.add((IrConstant) val);
//                        }
                        gv = new IrGlobalVariable(def.identName, type, inits, true);
                    } else {
//                        IrValue initVal = def.initialValue.accept(this).value;
//                        if (initVal instanceof IrConstant) {
//                            gv = new IrGlobalVariable(def.identName, type, (IrConstant) initVal, true);
//                        } else {
//                            throw new RuntimeException("全局变量 " + def.identName + " 的初始值不是一个常量!");
//                        }
                        gv = new IrGlobalVariable(def.identName, type, new IrConstant(((VarSymbol)sym).constValue), true);
                    }

                } else {
                    throw new RuntimeException("const initialValue cannot be null");
                }
                module.addGlobalVariable(gv);
                valueMap.put(sym, gv); // 映射: "g_a" -> "@g_a" (指针)
            } else {
                // 局部变量
                // alloca: %a.ptr = alloca i32
                IrAlloca ptr = new IrAlloca(type);
                addInstr(ptr);
                // 存入 map: "a" -> %a.ptr
                valueMap.put(sym, ptr);

                // 处理初始化
                if (def.initialValue != null) {
                    if (!def.initialValue.isArray) {
                        // store i32 [initVal], i32* %a.ptr
                        IrValue initVal = def.initialValue.accept(this).value;
                        addInstr(new IrStore(initVal, ptr));
                    } else {
                        // 数组初始化
                        List<ExprNode> inits = def.initialValue.arrayInits;
                        for (int i = 0; i < inits.size(); i++ ) {
                            IrValue initVal = inits.get(i).accept(this).value;
                            // 获取a[i]的地址
                            IrGetElementPtr gep = new IrGetElementPtr(ptr, new IrConstant(i));
                            IrValue targetPtr = addInstr(gep);
                            // store
                            addInstr(new IrStore(initVal, targetPtr));
                        }
                    }

                }
            }
        }
        return null;
    }

    @Override
    public VisitResult visit(VarDecl node) {
        for (VarDef def : node.varDefs) {
            Symbol sym = currentTable.getSymbol(def.identName);
            IrType type = sym.getIrType();
            String globalName;

            boolean ifStatic = (sym instanceof VarSymbol) && (((VarSymbol) sym).btype == 2);
            if (currentFunction == null || ifStatic) {
                // 一定是静态局部变量
                if (currentFunction != null && ifStatic) {
                    String funcName = currentFunction.name.substring(1);
                    globalName = funcName + "_" + def.identName + "_" + staticVarCounter++;
                } else {
                    // 全局变量不能重名
                    globalName = "g_" + def.identName;
                }
                // 全局变量的初始化应该是这样的@a = dso_local global [6 x i32] [i32 1, i32 2, i32 3, i32 4, i32 5, i32 6]
                IrGlobalVariable gv;

                // initValue不可能为空，如果是全局的话必须初始值为const
                if (def.initialValue != null) {
                    if (def.initialValue.isArray) {
                        List<IrConstant> inits = new ArrayList<>();
                        for (Integer init : ((VarSymbol)sym).constArrayValues) {
                            inits.add(new IrConstant(init));
                        }
                        gv = new IrGlobalVariable(globalName, type, inits, false);
                    } else {
                        gv = new IrGlobalVariable(globalName, type, new IrConstant(((VarSymbol)sym).constValue), false);
                    }

                } else {
                    // 直接初始化为0
                    gv = new IrGlobalVariable(globalName, type, (IrConstant) null, false);
                }
                module.addGlobalVariable(gv);
                valueMap.put(sym, gv); // 映射: "g_a" -> "@g_a" (指针)
            } else {
                // 局部变量
                // alloca: %a.ptr = alloca i32
                IrAlloca ptr = new IrAlloca(type);
                addInstr(ptr);
                // 存入 map: "a" -> %a.ptr
                valueMap.put(sym, ptr);

                // 处理初始化
                if (def.initialValue != null) {
                    if (!def.initialValue.isArray) {
                        // store i32 [initVal], i32* %a.ptr
                        IrValue initVal = def.initialValue.accept(this).value;
                        addInstr(new IrStore(initVal, ptr));
                    } else {
                        // 数组初始化
                        List<ExprNode> inits = def.initialValue.arrayInits;
                        for (int i = 0; i < inits.size(); i++ ) {
                            IrValue initVal = inits.get(i).accept(this).value;
                            // 获取a[i]的地址
                            IrGetElementPtr gep = new IrGetElementPtr(ptr, new IrConstant(i));
                            IrValue targetPtr = addInstr(gep);
                            // store
                            addInstr(new IrStore(initVal, targetPtr));
                        }
                    }

                }
            }
        }
        return null;
    }

    @Override
    public VisitResult visit(VarDef varDef) {
        return null;
    }

    @Override
    public VisitResult visit(ConstDef node) {
        return null;
    }

    @Override
    public VisitResult visit(InitVal node) {
        if (node.isArray) {
            return new VisitResult();
        } else {
            return node.singleInit.accept(this);
        }
    }

    @Override
    public VisitResult visit(Block node) {
        boolean isNewScope = (node.scope != null); // 检查这是否是一个新作用域
        if (isNewScope) {
            this.currentTable = node.scope; // 进入新作用域
        }

        for (Node item : node.items) {
            item.accept(this);
            // 如果块已经终结 停止处理
            if (currentBlock.isTerminated()) {
                break;
            }
        }

        if (isNewScope) {
            this.currentTable = this.currentTable.preTable; // 退出作用域
        }
        return null;
    }

    @Override
    public VisitResult visit(AssignStmt node) {
        // 计算 RValue
        IrValue rValue = node.rValue.accept(this).value;
        // 找到 LValue 的指针
        IrValue lValuePtr = getLValPtr(node.lVal);
        // store
        addInstr(new IrStore(rValue, lValuePtr));
        return null;
    }

    @Override
    public VisitResult visit(IfStmt node) {
        // 创建 BasicBlock
        IrBasicBlock thenBlock = currentFunction.createBasicBlock(newLabel(""));
        IrBasicBlock elseBlock = (node.elseStmt != null) ? currentFunction.createBasicBlock(newLabel("")) : null;
        IrBasicBlock mergeBlock = currentFunction.createBasicBlock(newLabel(""));

        // 计算条件, 必须返回 i1
        IrValue condVal = node.condition.accept(this).value;
        condVal = convertOpToBool(condVal);

        // 添加 'br' (终结当前块)
        if (node.elseStmt != null) {
            addInstr(new IrBranch(condVal, thenBlock, elseBlock)); // br i1 %cmp, label %if.then, label %if.else
        } else {
            addInstr(new IrBranch(condVal, thenBlock, mergeBlock)); // br i1 %cmp, label %if.then, label %if.merge
        }

        // Then 块
        this.currentBlock = thenBlock;
        node.thenStmt.accept(this);
        if (!currentBlock.isTerminated()) {
            addInstr(new IrBranch(mergeBlock)); // 跳到 merge
        }

        // else 块
        if (node.elseStmt != null) {
            this.currentBlock = elseBlock;
            node.elseStmt.accept(this);
            if (!currentBlock.isTerminated()) {
                addInstr(new IrBranch(mergeBlock)); // 跳到 merge
            }
        }

        // 后续指令都在 merge 块中
        this.currentBlock = mergeBlock;

        return null;
    }

    @Override
    public VisitResult visit(ForStmt node) {
        // 访问 Init 在循环前的块中
        if (node.initStmts != null) {
            for (AssignStmt init : node.initStmts) init.accept(this);
        }

        // 创建 basicblock
        IrBasicBlock condBlock = currentFunction.createBasicBlock(newLabel(""));
        IrBasicBlock bodyBlock = currentFunction.createBasicBlock(newLabel(""));
        IrBasicBlock updateBlock = (node.updateStmts != null && !node.updateStmts.isEmpty()) ? currentFunction.createBasicBlock(newLabel("")) : condBlock;
        IrBasicBlock afterBlock = currentFunction.createBasicBlock(newLabel(""));

        // 当前块 -> cond
        addInstr(new IrBranch(condBlock));

        // Cond 块
        this.currentBlock = condBlock;
        if (node.condition != null) {
            IrValue condVal = node.condition.accept(this).value; // 必须是 i1
            condVal = convertOpToBool(condVal);
            addInstr(new IrBranch(condVal, bodyBlock, afterBlock)); // br i1 %cmp, label %for.body, label %for.after
        } else {
            // 直接跳转到body
            addInstr(new IrBranch(bodyBlock));
        }

        // Body 块
        this.currentBlock = bodyBlock;
        // 注册 break/continue 目标
        loopBreakStack.push(afterBlock);
        loopContinueStack.push(updateBlock);

        node.body.accept(this);

        loopBreakStack.pop();
        loopContinueStack.pop();

        if (!currentBlock.isTerminated()) {
            addInstr(new IrBranch(updateBlock)); // body -> update
        }

        // Update 块
        if (node.updateStmts != null) {
            this.currentBlock = updateBlock;
            for (AssignStmt update : node.updateStmts) update.accept(this);
            if (!currentBlock.isTerminated()) {
                addInstr(new IrBranch(condBlock)); // update -> cond
            }
        }

        // 后续指令都在 after 块中
        this.currentBlock = afterBlock;
        return null;
    }

    @Override
    public VisitResult visit(PrintfStmt node) {
        // 先把printf后面的表达式解析了，防止printf顺序出问题
        List<IrValue> args = new ArrayList<>();
        for (ExprNode arg : node.args) {
            args.add(arg.accept(this).value);
        }
        // 解析格式化字符串, 拆解成 putint, putch
        String formatString = node.formatString;
        int argIndex = 0;

        for (int i = 1; i < formatString.length() - 1; i++) {
            if (formatString.startsWith("%d", i)) {
                // 遇到 %d
                IrFunction putint = module.getFunction("putint");
                IrValue val = args.get(argIndex++);
                addInstr(new IrCall(putint, List.of(val)));
                i++; // 跳过 'd'
            } else {
                IrFunction putch = module.getFunction("putch");
                int c = formatString.charAt(i);
                // 只有一种转义情况需要考虑
                if (c == '\\' && i+1<formatString.length()) {
                    if (formatString.charAt(i+1) == 'n') {
                        addInstr(new IrCall(putch, List.of(new IrConstant('\n'))));
                        i++;
                    }
//                    } else if (formatString.charAt(i+1) == 't') {
//                        addInstr(new IrCall(putch, List.of(new IrConstant('\t'))));
//                        i++;
//                    } else if (formatString.charAt(i+1) == 'f') {
//                        addInstr(new IrCall(putch, List.of(new IrConstant('\f'))));
//                        i++;
//                    } else if (formatString.charAt(i+1) == '0') {
//                        addInstr(new IrCall(putch, List.of(new IrConstant('\0'))));
//                        i++;
//                    }
                } else
                    addInstr(new IrCall(putch, List.of(new IrConstant(c))));
            }
        }

        return null;
    }

    @Override
    public VisitResult visit(ReturnStmt node) {
        if (node.returnValue != null) {
            // 递归调用Exp
            VisitResult result = node.returnValue.accept(this);
            IrValue valueToReturn = result.value;
            // ret i32 1
            addInstr(new IrReturn(valueToReturn));

        } else {
            // ret void
            addInstr(new IrReturn());
        }
        return null;
    }

    @Override
    public VisitResult visit(BreakStmt node) {
        addInstr(new IrBranch(loopBreakStack.peek()));
        return null;
    }

    @Override
    public VisitResult visit(ContinueStmt node) {
        addInstr(new IrBranch(loopContinueStack.peek()));
        return null;
    }

    @Override
    public VisitResult visit(ExprStmt node) {
        if (node.expr != null) {
            node.expr.accept(this);
        }
        return null;
    }

    @Override
    public VisitResult visit(LVal node) {
        // 检查 LVal 本身是否为函数调用的数组
        Symbol sym = node.symbol;
        boolean isBaseArray = (sym instanceof VarSymbol)
                && ((VarSymbol) sym).isArray
                && (node.arrayIndex == null);
        // 找到 LVal 的指针
        IrValue ptr = getLValPtr(node);

        VisitResult vr = new VisitResult();
        if (isBaseArray) {
            //System.out.println(ptr.irType);
            // 说明是函数调用的数组，要转成指针输入
            IrGetElementPtr decayPtr = new IrGetElementPtr(ptr, IrConstant.ZERO);
            vr.value = addInstr(decayPtr);
        } else {
            // 如果是一个标量 a / a[1]
            // load: %t1 = load i32, i32* %a.ptr
            // 返回 load 的结果 (%t1)
            vr.value = addInstr(new IrLoad(ptr));
        }

        return vr;
    }

    @Override
    public VisitResult visit(BinaryOpExp node) {
        IrValue left = node.left.accept(this).value;

        // 用phi处理短路求值
        if (node.op == Operator.AND) {
            // A&&B
            IrBasicBlock checkRhsBlock = currentFunction.createBasicBlock(newLabel(""));
            IrBasicBlock finalBlock = currentFunction.createBasicBlock(newLabel(""));
            IrBasicBlock startBlock = currentBlock; // 记住我们从哪里开始

            // 在当前块分配一个临时变量存结果 (i32)
            IrAlloca resultPtr = new IrAlloca(IrType.getInt32());
            addInstr(resultPtr);

            // 访问 A, 并将其转为 i1
            IrValue leftBool = convertOpToBool(left);
            // 如果左值为假，则结果为0，存入 resultPtr，跳到 final
            IrBasicBlock shortCircuitBlock = currentFunction.createBasicBlock(newLabel(""));
            addInstr(new IrBranch(leftBool, checkRhsBlock, shortCircuitBlock));

            // 填充 ShortCircuit 块 (左为假)
            this.currentBlock = shortCircuitBlock;
            addInstr(new IrStore(IrConstant.ZERO, resultPtr)); // store 0
            addInstr(new IrBranch(finalBlock));

            // B(RHS 块
            this.currentBlock = checkRhsBlock;
            IrValue rightVal = node.right.accept(this).value;
            IrValue rightBool = convertOpToBool(rightVal);

            IrValue rightI32 = addInstr(new IrZext(rightBool, IrType.getInt32()));
            addInstr(new IrStore(rightI32, resultPtr));
            addInstr(new IrBranch(finalBlock)); // B -> final

            // final块
            this.currentBlock = finalBlock;
            // 从内存中加载最终结果
            IrValue result = addInstr(new IrLoad(resultPtr));
            return new VisitResult(result);
            //IrPhi phi = new IrPhi(IrType.getInt1());

            // 如果 A=false, 结果是 false (i1 0)
            //phi.addIncoming(IrConstant.FALSE, startBlock);
            // "如果 A=true, 结果是 B 的值
            //phi.addIncoming(rightBool, rhsEndBlock);
            // phi 指令本身会产生一个 IrValue, 这就是 A && B 的最终结果
            //return new VisitResult(addInstr(phi));
        } else if (node.op == Operator.OR) {
            IrBasicBlock checkRhsBlock = currentFunction.createBasicBlock(newLabel(""));
            IrBasicBlock finalBlock = currentFunction.createBasicBlock(newLabel(""));
            IrBasicBlock startBlock = currentBlock;

            IrAlloca resultPtr = new IrAlloca(IrType.getInt32());
            addInstr(resultPtr);

            IrValue leftBool = convertOpToBool(left);

            IrBasicBlock shortCircuitBlock = currentFunction.createBasicBlock(newLabel(""));
            addInstr(new IrBranch(leftBool, shortCircuitBlock, checkRhsBlock));

            // 填充 ShortCircuit 块 (左为真)
            this.currentBlock = shortCircuitBlock;
            addInstr(new IrStore(new IrConstant(1), resultPtr)); // store 1
            addInstr(new IrBranch(finalBlock));

            // 填充 CheckRhs 块 (左为假)
            this.currentBlock = checkRhsBlock;
            IrValue rightVal = node.right.accept(this).value;
            IrValue rightBool = convertOpToBool(rightVal);

            // Store 右值
            IrValue rightI32 = addInstr(new IrZext(rightBool, IrType.getInt32()));
            addInstr(new IrStore(rightI32, resultPtr));
            addInstr(new IrBranch(finalBlock));

            // 进入final块
            this.currentBlock = finalBlock;
            IrValue result = addInstr(new IrLoad(resultPtr));
            return new VisitResult(result);
//            IrPhi phi = new IrPhi(IrType.getInt1());
//            // A=true 结果就是true
//            phi.addIncoming(IrConstant.TRUE, startBlock);
//            // A=false 就还要再算B的值
//            phi.addIncoming(rightBool, rhsEndBlock);
//            return new VisitResult(addInstr(phi));
        }
        IrValue right = node.right.accept(this).value;
        // 如果存在i1类型都转成i32
        if (left.irType.equals(IrType.getInt1())) {
            left = addInstr(new IrZext(left, IrType.getInt32()));
        }
        if (right.irType.equals(IrType.getInt1())) {
            right = addInstr(new IrZext(right, IrType.getInt32()));
        }
        IrInstruction op;
        switch (node.op) {
            case ADD: op = new IrBinaryOp("add", left, right); break;
            case SUB: op = new IrBinaryOp("sub", left, right); break;
            case MUL: op = new IrBinaryOp("mul", left, right); break;
            case DIV: op = new IrBinaryOp("sdiv", left, right); break;
            case MOD: op = new IrBinaryOp("srem", left, right); break;

            // 关系运算必须生成 'icmp' (返回 i1)
            case LT: op = new IrCompare("slt", left, right); break;
            case EQ: op = new IrCompare("eq", left, right); break;
            case NE: op = new IrCompare("ne", left, right); break;
            case LE: op = new IrCompare("sle", left, right); break;
            case GT: op = new IrCompare("sgt", left, right); break;
            case GE: op = new IrCompare("sge", left, right); break;

            default: throw new RuntimeException("未知的二元运算符: " + node.op);
        }
        VisitResult vr = new VisitResult();
        vr.value = addInstr(op);
        return vr;
    }

    @Override
    public VisitResult visit(UnaryExp node) {
        IrValue operand = node.operand.accept(this).value;
        IrInstruction op;
        VisitResult vr = new VisitResult();
        switch (node.op) {
            case POS:
                // 'a = +b' -> 'a = b' (在 IR 中, 'add 0, b' 是一种方式, 或直接返回 b)
                vr.value = operand;
                return vr;
            case NEG:
                // 'a = -b' -> 'sub i32 0, b'
                op = new IrBinaryOp("sub", IrConstant.ZERO, operand);
                break;
            case NOT:
                // 'a = !b' -> 'icmp eq i32 b, 0' (如果 b==0, 则 !b 为 true(1))
                op = new IrCompare("eq", operand, IrConstant.ZERO);
                break;
            default:
                throw new RuntimeException("未知的 UnaryOp");
        }
        vr.value = addInstr(op);
        return vr;
    }

    @Override
    public VisitResult visit(FuncCallExp node) {
        IrFunction func = module.getFunction(node.funcName);

        // 计算所有参数
        List<IrValue> args = new ArrayList<>();
        for (ExprNode argNode : node.args) {
            args.add(argNode.accept(this).value);
        }

        IrInstruction call = (IrInstruction) addInstr(new IrCall(func, args));
        // call 本身是一个 IrValue, 需要返回
        VisitResult vr = new VisitResult();
        vr.value = call;
        return vr;
    }

    @Override
    public VisitResult visit(Number number) {
        VisitResult vr = new VisitResult();
        vr.value = new IrConstant(number.value);
        return vr;
    }
}
