package sysy.middle.ir;

public class IrIntegerType extends IrType {
    public final int bitWidth;

    protected IrIntegerType(int bitWidth) {
        super(TypeID.INTEGER);
        this.bitWidth = bitWidth;
    }

    @Override
    public String toString() {
        return "i" + bitWidth; // "i32", "i1"
    }
}
