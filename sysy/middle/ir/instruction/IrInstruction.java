package sysy.middle.ir.instruction;

import sysy.middle.ir.IrNode;
import sysy.middle.ir.type.IrType;
import sysy.middle.ir.value.IrValue;

import java.util.List;

// 所有指令的抽象类
public abstract class IrInstruction extends IrValue implements IrNode {

    public IrBasicBlock parentBlock;
    // 指令产生值的类型
    public IrInstruction(IrType type) {
        super(type);
    }

    // 生成指令的左值，如果不产生值就只返回缩进
    protected String getLHS() {
        String indent = "  "; // 所有指令都缩进
        if (this.irType.isVoid()) {
            return indent; // e.g., 'store', 'ret', 'br'
        } else {
            // e.g., 'add', 'load', 'alloca'
            return indent + this.name + " = ";
        }
    }

    // 返回 "i32 %t1"
    @Override
    public String getOperandString() {
        return irType.toString() + " " + this.name;
    }

    // 返回 "%t1"
    @Override
    public String getNameOrConst() {
        return this.name;
    }

    // irOutput() 必须由每个具体的子类来实现
    @Override
    public abstract List<String> irOutput();
}
// 标记终结符
interface IrTerminator { }