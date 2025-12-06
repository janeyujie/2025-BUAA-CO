package sysy.middle.ir.instruction;

import sysy.middle.ir.type.IrPointerType;
import sysy.middle.ir.type.IrType;
import sysy.middle.ir.value.IrValue;
import sysy.middle.ir.type.IrArrayType;

import java.util.List;

public class IrGetElementPtr extends IrInstruction {
    public IrValue pointer; // 基指针
    public IrValue index;   // 索引

    // GEP 有两种模式, GEP 必须知道它在索引什么类型
    public IrType pointerType; // 指针的原始类型 [10 x i32]*, i32*
    public IrType baseType;    // 指针指向的类型 [10 x i32], i32

    public IrGetElementPtr(IrValue pointer, IrValue index) {
        super(calResultType(pointer));
        this.pointer = pointer;
        this.index = index;
        this.baseType = ((IrPointerType) pointer.irType).baseType;
    }

    private static IrType calResultType(IrValue pointer) {
        if (!(pointer.irType instanceof IrPointerType)) {
            throw new IllegalArgumentException("GEP 必须接收一个指针类型! 但收到了: " + pointer.irType);
        }
        // 获取指针指向的类型 (Base Type)
        IrType baseType = ((IrPointerType) pointer.irType).baseType;

        // 计算GEP的结果类型
        if (baseType instanceof IrArrayType) {
            // GEP on "[10 x i32]*" (数组指针)
            // ... 结果是 "i32*" (元素指针)
            return IrType.getPointer(((IrArrayType) baseType).baseType);
        } else {
            // GEP on "i32*" (普通指针)
            // ... 结果还是 "i32*" (元素指针)
            return IrType.getPointer(baseType);
        }
    }

    @Override
    public List<String> irOutput() {
        String line;

        if (baseType instanceof IrArrayType) {
            // 数组指针 a[10] -> a[i]
            // getelementptr inbounds [10 x i32], [10 x i32]* %a.ptr, i32 0, i32 %i
            line = getLHS() + "getelementptr inbounds " + baseType.toString() + ", " +
                    pointer.toString() + ", i32 0, " + index.toString();
        } else {
            // 函数参数传递指针 a[] -> a[i]
            // getelementptr inbounds i32, i32* %a, i32 %i
            line = getLHS() + "getelementptr inbounds " + baseType.toString() + ", " +
                    pointer.toString() + ", " + index.toString();
        }

        return List.of(line);
    }
}
