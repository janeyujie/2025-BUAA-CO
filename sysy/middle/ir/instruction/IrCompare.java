package sysy.middle.ir.instruction;

import sysy.middle.ir.type.IrType;
import sysy.middle.ir.value.IrValue;

import java.util.List;

// %cmp = icmp slt i32 %a, %b"
public class IrCompare extends IrInstruction {

    public String conditionCode; // "eq", "ne", "slt", "sle", "sgt", "sge"
    public IrValue left;
    public IrValue right;

    public IrCompare(String condCode, IrValue left, IrValue right) {
        super(IrType.getInt1()); // icmp 产生 i1
        this.conditionCode = condCode;
        this.left = left;
        this.right = right;
    }

    @Override
    public List<String> irOutput() {
        String line = getLHS() + "icmp " + conditionCode + " " + left.toString() + ", " + right.getNameOrConst();
        return List.of(line);
    }
}
