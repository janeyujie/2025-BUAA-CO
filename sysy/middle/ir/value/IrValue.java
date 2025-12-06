package sysy.middle.ir.value;

import sysy.middle.ir.type.IrType;

public abstract class IrValue {
    public IrType irType; // 值的类型
    public String name; // 值在 LLVM 中的名字

    public IrValue(IrType irType) {
        this.irType = irType;
    }

    // 获取 类型+值
    public abstract String getOperandString();

    // 获取值
    public abstract String getNameOrConst();

    @Override
    public final String toString() {
        return getOperandString();
    }
}
