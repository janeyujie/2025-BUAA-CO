package sysy.middle.ir;

import java.util.HashMap;
import java.util.Map;

public class IrType {

    public enum TypeID {
        VOID,  // void
        INTEGER,  // i1, i32
        LABEL, // 代码块标签
        POINTER,  // 变量类型
        ARRAY  // 数组类型
    }

    protected final TypeID typeID;

    // 用于单例的静态实例
    private static final IrType VOID_TYPE = new IrType(TypeID.VOID);
    private static final IrType LABEL_TYPE = new IrType(TypeID.LABEL);
    // 整数类型 (i32, i1)
    private static final IrType I32_TYPE = new IrIntegerType(32);
    private static final IrType I1_TYPE = new IrIntegerType(1); // 用于 'icmp' (比较) 的布尔值

    protected IrType(TypeID typeID) {
        this.typeID = typeID;
    }

    // 静态工厂方法Getters
    public static IrType getVoid() { return VOID_TYPE; }
    public static IrType getLabel() { return LABEL_TYPE; }
    public static IrType getInt32() { return I32_TYPE; }
    public static IrType getInt1() { return I1_TYPE; } // 'icmp' 会产生 i1

    // 复杂类型工厂方法(指针和数组)
    // 使用 Map 来重用指针类型
    private static final Map<IrType, IrPointerType> pointerCache = new HashMap<>();

    public static IrPointerType getPointer(IrType baseType) {
        // 如果我们没有缓存过这个类型的指针, 创建一个新的并缓存它
        return pointerCache.computeIfAbsent(baseType, IrPointerType::new);
    }

    public static IrArrayType getArray(IrType baseType, int size) {
        // 数组类型通常不重用, 因为大小可能不同
        return new IrArrayType(baseType, size);
    }

    public boolean isVoid() { return this.typeID == TypeID.VOID; }

    // toString() 必须返回 LLVM 语法
    @Override
    public String toString() {
        switch (typeID) {
            case VOID: return "void";
            case LABEL: return "label";
            default: return "unknown_type";
        }
    }
}
