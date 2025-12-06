package sysy.backend;

import sysy.backend.MipsInstruction.*;
import sysy.middle.ir.*;
import sysy.middle.ir.instruction.*;
import sysy.middle.ir.type.IrArrayType;
import sysy.middle.ir.type.IrPointerType;
import sysy.middle.ir.type.IrType;
import sysy.middle.ir.value.*;

public class MipsBuilder {
    private MipsModule mipsModule = new MipsModule();
    private MipsFunc currentFunc;
    private MipsBasicBlock currentBlock;
    private MipsStackFrame currentFrame;

    // 字符串合并缓冲
    private StringBuilder strBuffer = new StringBuilder();
    private int strCounter = 0;

    public MipsModule build(IrModule module) {
        // 处理全局变量 -> 分配 $gp 偏移，生成 .data 或初始化代码
        for (IrGlobalVariable gv : module.globalVariables) {
            String label = gv.name.substring(1);
            StringBuilder content = new StringBuilder();
            if (!(gv.irType instanceof IrPointerType)) {
                throw new RuntimeException("Global variable type must be pointer: " + gv.name);
            }
            IrType pt = ((IrPointerType) gv.irType).baseType;
            if (pt instanceof IrArrayType) {
                // 数组
                IrArrayType arrayType = (IrArrayType) pt;
                int size = arrayType.size * 4;

                if (gv.arrayInitializer != null && !gv.arrayInitializer.isEmpty()) {
                    // 一定有初始化：.word 1, 2, 3, 0
                    content.append(".word ");
                    for (int i = 0; i < gv.arrayInitializer.size(); i++) {
                        content.append(gv.arrayInitializer.get(i).value);
                        if (i < gv.arrayInitializer.size() - 1) {
                            content.append(", ");
                        }
                    }
                } else {
                    // 无初始化的情况下使用 .space
                    content.append(".space ").append(size);
                }
            } else {
                // int
                int val = 0;
                if (gv.scalarInitializer != null) {
                    val = gv.scalarInitializer.value;
                }
                content.append(".word ").append(val);
            }
            mipsModule.dataList.add(new MipsData(label, content.toString(), false));
        }


        // 处理函数调用
        for (IrFunction irFunc : module.functions) {
            if (irFunc.isDeclaration) continue;
            buildFunc(irFunc);
        }
        return mipsModule;
    }

    private void buildFunc(IrFunction irFunc) {
        currentFunc = new MipsFunc(irFunc.name.substring(1)); // 去掉@
        currentFrame = new MipsStackFrame();

        // 计算函数运行过程中需要的栈帧
        int stackSize = 4;
        // 遍历函数的形参列表
        for (IrValue param : irFunc.parameters) {
            currentFrame.alloc(param, 4);
            stackSize += 4;
        }
        // 为产生值的指令分配栈空间
        for (IrBasicBlock bb : irFunc.basicBlocks) {
            for (IrInstruction inst : bb.instructions) {
                if (inst instanceof IrAlloca) {
                    // 如果是数组的话得额外分配
                    IrAlloca alloc = (IrAlloca) inst;
                    int size = 4;
                    if (alloc.allocatedType instanceof IrArrayType) {
                        size = ((IrArrayType) alloc.allocatedType).size * 4;
                    }
                    currentFrame.alloc(inst, size);
                    stackSize += size;
                } else if (!inst.irType.isVoid()) {
                    currentFrame.alloc(inst, 4);
                    stackSize += 4;
                }
            }
        }
        // Align stack size to 8 bytes if needed (MIPS O32 requires 8-byte alignment for
        // double, but 4 is usually fine for int)
        // Let's keep it simple.
        currentFunc.stackSize = stackSize;

        // addiu $sp, $sp, -stackSize
        // sw $ra, stackSize-4($sp)
        MipsBasicBlock prologue = new MipsBasicBlock("");
        prologue.addInstr(new MipsBinaryImm("addiu", MipsReg.SP, MipsReg.SP, -stackSize));
        prologue.addInstr(new MipsMem("sw", MipsReg.RA, MipsReg.SP, stackSize - 4));
        // 把寄存器的参数存到栈上
        int paramCount = irFunc.parameters.size();
        for (int i = 0; i < paramCount; i++) {
            IrValue param = irFunc.parameters.get(i);
            MipsSymbol sym = currentFrame.get(param);
            if (i < 4) {
                prologue.addInstr(new MipsMem("sw", getArgReg(i), MipsReg.SP, sym.offset));
            } else {
                // 第5个及以后的参数已经在调用者的栈帧里了
                int callerOffset = stackSize + (i - 4) * 4;
                // 从调用者栈帧加载
                prologue.addInstr(new MipsMem("lw", MipsReg.T0, MipsReg.SP, callerOffset));
                // 存到自己的栈帧
                prologue.addInstr(new MipsMem("sw", MipsReg.T0, MipsReg.SP, sym.offset));
            }
        }
        currentFunc.blocks.add(prologue);

        // 生成函数体的指令
        for (IrBasicBlock bb : irFunc.basicBlocks) {
            buildBlock(bb);
        }
        mipsModule.funcList.add(currentFunc);
    }

    private void buildBlock(IrBasicBlock bb) {
        currentBlock = new MipsBasicBlock(bb.name); // IR label 需要处理一下名字

        for (IrInstruction inst : bb.instructions) {
            // --- 字符串合并逻辑 ---
            if (isPutch(inst)) {
                int charCode = getPutchChar(inst);
                strBuffer.append((char) charCode);
                continue; // 暂不生成指令
            } else {
                if (!strBuffer.isEmpty()) {
                    flushStringBuffer(); // 生成打印字符串的 syscall
                }
            }
            buildInstr(inst);
        }
        // 块结束时也要 flush
        if (strBuffer.length() > 0)
            flushStringBuffer();
        currentFunc.blocks.add(currentBlock);
    }

    private void buildInstr(IrInstruction inst) {
        if (inst instanceof IrReturn) {
            IrReturn ret = (IrReturn) inst;
            // 1. Set return value
            if (ret.value != null) {
                loadToReg(ret.value, MipsReg.V0);
            }

            // 2. Restore $ra
            currentBlock.addInstr(new MipsMem("lw", MipsReg.RA, MipsReg.SP, currentFunc.stackSize - 4));
            // 3. Restore $sp
            currentBlock.addInstr(new MipsBinaryImm("addiu", MipsReg.SP, MipsReg.SP, currentFunc.stackSize));
            // 4. Return
            currentBlock.addInstr(new MipsJr(MipsReg.RA));
        } else if (inst instanceof IrBinaryOp) {
            IrBinaryOp bin = (IrBinaryOp) inst;

            loadToReg(bin.left, MipsReg.T0);
            loadToReg(bin.right, MipsReg.T1);
            if (bin.opCode.equals("sdiv")) {
                // 除法: div $t0, $t1 -> mflo $t2
                currentBlock.addInstr(new MipsBinary("div", MipsReg.T0, MipsReg.T1));
                currentBlock.addInstr(new MipsBinary("mflo", MipsReg.T2));
            } else if (bin.opCode.equals("srem")) {
                // 取模: div $t0, $t1 -> mfhi $t2
                currentBlock.addInstr(new MipsBinary("div", MipsReg.T0, MipsReg.T1));
                currentBlock.addInstr(new MipsBinary("mfhi", MipsReg.T2));
            } else {
                // 其他二元运算(考虑大数字的乘法？
                String mipsOp = mapOp(bin.opCode);
                currentBlock.addInstr(new MipsBinary(mipsOp, MipsReg.T2, MipsReg.T0, MipsReg.T1));
            }
            // 获取计算结果到$t2
            storeFromReg(inst, MipsReg.T2);

        } else if (inst instanceof IrCompare) {
            IrCompare cmp = (IrCompare) inst;

            loadToReg(cmp.left, MipsReg.T0);
            loadToReg(cmp.right, MipsReg.T1);
            String mipsOp = mapOp(cmp.conditionCode);
            currentBlock.addInstr(new MipsBinary(mipsOp, MipsReg.T2, MipsReg.T0, MipsReg.T1));
            storeFromReg(inst, MipsReg.T2);

        } else if (inst instanceof IrCall) {
            buildFuncCall((IrCall) inst);
        } else if (inst instanceof IrStore) {
            IrStore store = (IrStore) inst;
            loadToReg(store.value, MipsReg.T0);
            loadToReg(store.pointer, MipsReg.T1);
            // 写入内存: sw $t0, 0($t1)
            currentBlock.addInstr(new MipsMem("sw", MipsReg.T0, MipsReg.T1, 0));
            //storeFromReg(store.pointer, MipsReg.T0);
        } else if (inst instanceof IrLoad) {
            IrLoad load = (IrLoad) inst;
            loadToReg(load.pointer, MipsReg.T1);
            // 读取内存: lw $t0, 0($t1) -> $t0
            currentBlock.addInstr(new MipsMem("lw", MipsReg.T0, MipsReg.T1, 0));
            storeFromReg(load, MipsReg.T0);
        } else if (inst instanceof IrBranch) {
            IrBranch branch = (IrBranch) inst;
            // 无条件跳转
            if (branch.condition == null) {
                currentBlock.addInstr(new MipsJump("j", branch.trueTarget.name));
            } else {
                // 有条件跳转 if (cond!=0) goto truelabel else goto falselabel
                loadToReg(branch.condition, MipsReg.T0);
                // if (cond!=0) goto truelabel
                currentBlock.addInstr(new MipsBranch("bne", MipsReg.T0, MipsReg.ZERO, branch.trueTarget.name));
                // else goto falselabel
                currentBlock.addInstr(new MipsJump("j", branch.falseTarget.name));
            }
        } else if (inst instanceof IrGetElementPtr) {
            IrGetElementPtr gep = (IrGetElementPtr) inst;
            // 加载基地址 $t0
            //    - 如果是全局数组，loadToReg 会生成 la
            //    - 如果是局部数组，loadToReg 会生成 addiu $sp
            //    - 如果是数组参数，loadToReg 会生成 lw (取出指针值)
            loadToReg(gep.pointer, MipsReg.T0);
            loadToReg(gep.index, MipsReg.T1);
            // 计算偏移字节 $t1 = index * 4
            currentBlock.addInstr(new MipsBinaryImm("sll", MipsReg.T1, MipsReg.T1, 2));
            // 计算最终地址 $t2 = base + offset
            currentBlock.addInstr(new MipsBinary("addu", MipsReg.T2, MipsReg.T0, MipsReg.T1));
            storeFromReg(inst, MipsReg.T2);
        } else if (inst instanceof IrZext zext) {
            loadToReg(zext.valueToExt, MipsReg.T0);
            // i1 (0/1) 已经是 32 位表示了，直接存回去即可
            storeFromReg(inst, MipsReg.T0);
        }
        // ... 处理其他指令
    }

    private void flushStringBuffer() {
        String label = "str_" + strCounter++;
        mipsModule.dataList.add(new MipsData(label, strBuffer.toString(), true));
        strBuffer.setLength(0); // 清空

        // 生成 MIPS: la $a0, label; li $v0, 4; syscall
        currentBlock.addInstr(new MipsLa(MipsReg.A0, label));
        currentBlock.addInstr(new MipsLi(MipsReg.V0, 4));
        currentBlock.addInstr(new MipsSyscall());
    }

    // 从栈或立即数加载到寄存器
    private void loadToReg(IrValue val, String reg) {
        if (val instanceof IrConstant) {
            // const
            // li $t0, 10
            currentBlock.addInstr(new MipsLi(reg, ((IrConstant) val).value));
        } else if(val instanceof IrGlobalVariable) {
            // 全局变量(也包括全局数组
            // 加载地址: la $reg, label, 只要取出地址就行
            String label = val.name.substring(1);
            currentBlock.addInstr(new MipsLa(reg, label));
            // 读取值: lw $reg, 0($t9)
            //currentBlock.addInstr(new MipsMem("lw", reg, MipsReg.T9, 0));
        } else if (val instanceof IrAlloca){
            // address = $sp + offset
            MipsSymbol sym = currentFrame.get(val);
            currentBlock.addInstr(new MipsBinaryImm("addiu", reg, MipsReg.SP, sym.offset));
        }else {
            // 局部变量/形参 reg = *($sp + offset)
            MipsSymbol sym = currentFrame.get(val);
            currentBlock.addInstr(new MipsMem("lw", reg, MipsReg.SP, sym.offset));
        }
    }

    private void storeFromReg(IrValue val, String reg) {
        if (val instanceof IrGlobalVariable) {
            // 加载地址
            currentBlock.addInstr(new MipsLa(MipsReg.T9, val.name.substring(1)));
            // 写入值
            currentBlock.addInstr(new MipsMem("sw", reg, MipsReg.T9, 0));
        } else {
            MipsSymbol sym = currentFrame.get(val);
            currentBlock.addInstr(new MipsMem("sw", reg, MipsReg.SP, sym.offset));
        }
    }

    // Helpers
    private boolean isPutch(IrInstruction inst) {
        if (inst instanceof IrCall) {
            IrCall call = (IrCall) inst;
            return call.functionToCall.name.equals("@putch");
        }
        return false;
    }

    private int getPutchChar(IrInstruction inst) {
        IrCall call = (IrCall) inst;
        IrValue arg = call.arguments.get(0);
        if (arg instanceof IrConstant) {
            return ((IrConstant) arg).value;
        }
        return 0; // Should not happen for string literals
    }

    private String mapOp(String irOp) {
        switch (irOp) {
            case "add":
                return "addu";
            case "sub":
                return "subu";
            case "mul":
                return "mul";
            case "sdiv":
                return "div"; // div $t0, $t1 -> mflo $t2 (need special handling for div)
            // For now, let's assume simple mapping or handle div specially if needed.
            // MIPS 'div' instruction stores result in HI/LO.
            // We need 'mflo' to get quotient.
            // But MipsBinary assumes 3 operands.
            // Let's just return "addu" for now as placeholder if not add/sub/mul,
            // or better, handle div in buildInstr if we want to be correct.
            // But for 'main return 0', we don't need div.
            case "slt": return "slt";
            case "sle": return "sle";
            case "sgt": return "sgt";
            case "sge": return "sge";
            case "eq":  return "seq";
            case "ne":  return "sne";
            default:
                throw new RuntimeException("Unknown op" + irOp);
        }
    }

    private void buildFuncCall(IrCall call) {
        String funcName = call.functionToCall.name;
        if (funcName.equals("@putint")) {
            // putint(int)
            loadToReg(call.arguments.get(0), MipsReg.A0);
            // li $v0, 1
            currentBlock.addInstr(new MipsLi(MipsReg.V0, 1));
            currentBlock.addInstr(new MipsSyscall());
        } else if (funcName.equals("@getint")) {
            // getint() -> int
            // li $v0, 5
            currentBlock.addInstr(new MipsLi(MipsReg.V0, 5));
            currentBlock.addInstr(new MipsSyscall());
            // Store $v0 to result (stack)
            storeFromReg(call, MipsReg.V0);
        } else if (funcName.equals("@putch")) {
            loadToReg(call.arguments.get(0), MipsReg.A0);
            // li $v0, 11 (print_char)
            currentBlock.addInstr(new MipsLi(MipsReg.V0, 11));
            currentBlock.addInstr(new MipsSyscall());
        } else {
            // 标准函数调用
            // 计算需要压栈的参数空间（超过4个参数
            int argCount = call.arguments.size();
            int extraArgs = Math.max(0, argCount - 4);
            int argsStackSize = extraArgs * 4;

            // 调整栈指针
            if (argsStackSize > 0) {
                currentBlock.addInstr(new MipsBinaryImm("addiu", MipsReg.SP, MipsReg.SP, -argsStackSize));
            }

            for (int i = 0; i < argCount; i++) {
                IrValue arg = call.arguments.get(i);

                // 如果要加载的是局部变量，且栈指针已调整，需要修正偏移
                loadToRegWithOffset(arg, MipsReg.T0, argsStackSize);

                if (i < 4) {
                    // 前四个参数 $a0->$a3 move $ax, $t0
                    currentBlock.addInstr(new MipsBinary("addu", getArgReg(i), MipsReg.T0, MipsReg.ZERO));
                } else {
                    // 后续参数从栈上获取
                    // sw $t0, offset($sp)
                    int argOffset = (i - 4) * 4;
                    currentBlock.addInstr(new MipsMem("sw", MipsReg.T0, MipsReg.SP, argOffset));
                }
            }

            // 跳转到函数
            currentBlock.addInstr(new MipsJump("jal", funcName.substring(1)));

            // 恢复栈指针
            if (argsStackSize > 0) {
                currentBlock.addInstr(new MipsBinaryImm("addiu", MipsReg.SP, MipsReg.SP, argsStackSize));
            }

            // 处理返回值
            if (!call.irType.isVoid()) {
                storeFromReg(call, MipsReg.V0);
            }
        }
    }

    private String getArgReg(int index) {
        switch (index) {
            case 0: return MipsReg.A0;
            case 1: return MipsReg.A1;
            case 2: return MipsReg.A2;
            case 3: return MipsReg.A3;
        }
        return "";
    }

    // 当 $sp 向下移动了 stackCorrection 字节后，原有的局部变量偏移量需要 +stackCorrection 才能找到
    private void loadToRegWithOffset(IrValue val, String reg, int stackCorrection) {
        if (val instanceof IrConstant) {
            currentBlock.addInstr(new MipsLi(reg, ((IrConstant) val).value));
        } else {
            MipsSymbol sym = currentFrame.get(val);
            String base = sym.isGlobal ? MipsReg.GP : MipsReg.SP;

            int realOffset = sym.offset;
            // 只有当访问的是栈(局部变量)且栈指针被移动了，才需要修正
            if (!sym.isGlobal && stackCorrection != 0) {
                realOffset += stackCorrection;
            }

            currentBlock.addInstr(new MipsMem("lw", reg, base, realOffset));
        }
    }
}
