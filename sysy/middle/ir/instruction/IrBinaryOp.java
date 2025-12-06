package sysy.middle.ir.instruction;

import sysy.middle.ir.type.IrType;
import sysy.middle.ir.value.IrValue;

import java.util.List;

//  %t1 = add i32 %t0, 1
public class IrBinaryOp extends IrInstruction {

    public String opCode; // "add", "sub", "mul", "sdiv"
    public IrValue left;
    public IrValue right;

    public IrBinaryOp(String opCode, IrValue left, IrValue right) {
        super(IrType.getInt32());
        this.opCode = opCode;
        this.left = left;
        this.right = right;
    }

    @Override
    public List<String> irOutput() {
        String line = getLHS() + opCode + " " + left.irType.toString() + " " + left.getNameOrConst() + ", " + right.getNameOrConst();
        return List.of(line);
    }
}
