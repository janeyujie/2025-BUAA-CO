package sysy.middle.ir;

import java.util.List;

public class IrLoad extends IrInstruction {
    public IrValue pointer; // 要从中加载的地址

    // pointer
    public IrLoad(IrValue pointer) {

        // load 指令本身的类型 是它加载的指针的baseType
        super(check(pointer));
        this.pointer = pointer;
    }

    @Override
    public List<String> irOutput() {
        // %t1 = load i32* %a.ptr
        String line = getLHS() + "load " + irType.toString() + ", " + pointer.toString();

        return List.of(line);
    }
    private static IrType check(IrValue pointer) {
        // 检查类型是否正确
        if (!(pointer.irType instanceof IrPointerType)) {
            throw new IllegalArgumentException("IrLoad 必须接收一个指针类型!");
        }
        IrPointerType ptrType = (IrPointerType) pointer.irType;
        return ptrType.baseType;
    }
}
