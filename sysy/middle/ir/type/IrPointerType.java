package sysy.middle.ir.type;

public class IrPointerType extends IrType {
    public final IrType baseType; // 指针指向的类型

    public IrPointerType(IrType baseType) {
        super(TypeID.POINTER);
        this.baseType = baseType;
    }

    @Override
    public String toString() {
        return baseType.toString() + "*"; // e.g., "i32*", "i8*"
    }
}
