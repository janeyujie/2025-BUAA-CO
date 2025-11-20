package sysy.middle.ir;


// 表示一个常量
public class IrConstant extends IrValue {
    public final int value;
    public static final IrConstant ZERO = new IrConstant(0);
    public static final IrConstant TRUE = new IrConstant(1, IrType.getInt1());
    public static final IrConstant FALSE = new IrConstant(0, IrType.getInt1());

    public IrConstant(int value) {
        this(value, IrType.getInt32());
    }
    public IrConstant(int value, IrType type) {
        super(type);
        this.value = value;
    }

    // 返回 "i32 10"
    @Override
    public String getOperandString() {
        return irType.toString() + " " + this.value;
    }

    // 返回 "10"
    @Override
    public String getNameOrConst() {
        return String.valueOf(this.value);
    }
}
