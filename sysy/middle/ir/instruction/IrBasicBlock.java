package sysy.middle.ir.instruction;

import sysy.middle.ir.IrFunction;
import sysy.middle.ir.IrNode;
import sysy.middle.ir.type.IrType;
import sysy.middle.ir.value.IrValue;

import java.util.ArrayList;
import java.util.List;

// 基本块本身也是一个 IrValue, 因为它的“标签”可以被 'br' 指令引用
public class IrBasicBlock extends IrValue implements IrNode {

    public List<IrInstruction> instructions = new ArrayList<>();
    public IrFunction parentFunction;
    public IrBasicBlock(String name, IrFunction parent) {
        // 基本块的类型是 'label'
        super(IrType.getLabel());
        this.name = name; // IrValue 的 'name' 在这里就是标签名
        this.parentFunction = parent;
    }

    public void addInstruction(IrInstruction instr) {
        // 设置指令的父块，建立双向链接
        instr.parentBlock = this;
        this.instructions.add(instr);
    }


     //检查这个块是否已经有 终结指令
    public boolean isTerminated() {
        if (instructions.isEmpty()) return false;
        IrInstruction last = instructions.get(instructions.size() - 1);
        // IrTerminator 是 IrInstruction 的一个子接口/子类
        return last instanceof IrTerminator;
    }

    //@return e.g., "label %entry"
    @Override
    public String getOperandString() {
        // irType.toString() -> "label"
        // getNameOrConst() -> "%entry"
        return irType.toString() + " " + getNameOrConst();
    }

    @Override
    public String getNameOrConst() {
        // 在 LLVM IR 中, 标签作为操作数时, 必须以 '%' 开头
        return "%" + this.name;
    }

    @Override
    public List<String> irOutput() {
        List<String> output = new ArrayList<>();
        // 打印基本块的标签
        output.add(this.name + ":");
        // 递归调用所有指令的 irOutput()
        for (IrInstruction instr : instructions) {
            output.addAll(instr.irOutput());
        }

        return output;
    }
}
