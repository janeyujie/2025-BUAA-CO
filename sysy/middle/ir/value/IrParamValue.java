package sysy.middle.ir.value;

import sysy.middle.ir.type.IrType;

public class IrParamValue extends IrValue {

    public IrParamValue(IrType type, String name) {
        super(type);
        this.name = name;
    }

    @Override
    public String getOperandString() { return irType.toString() + " " + this.name; } // e.g., "i32 %arg"

    @Override
    public String getNameOrConst() { return this.name; } // e.g., "%arg"
}
