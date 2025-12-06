package sysy.middle.ir.instruction;

import sysy.middle.ir.type.IrType;
import sysy.middle.ir.value.IrValue;

import java.util.List;

public class IrZext extends IrInstruction {
    public IrValue valueToExt;
    public IrZext(IrValue valueToExt, IrType targetType) {
        super(targetType); // 结果是 i32
        this.valueToExt = valueToExt;
    }
    @Override
    public List<String> irOutput() {
        String line = getLHS() + "zext " + valueToExt.toString() + " to " + irType.toString();
        return List.of(line);
    }
}
