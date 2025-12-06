package sysy.middle.ir.type;

public class IrArrayType extends IrType {
    public final IrType baseType;
    public final int size;

    public IrArrayType(IrType baseType, int size) {
        super(TypeID.ARRAY);
        this.baseType = baseType;
        this.size = size;
    }

    @Override
    public String toString() {
        return "[" + size + " x " + baseType.toString() + "]";
    }
}
