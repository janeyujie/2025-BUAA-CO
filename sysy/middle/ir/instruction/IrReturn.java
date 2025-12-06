package sysy.middle.ir.instruction;

import sysy.middle.ir.type.IrType;
import sysy.middle.ir.value.IrValue;

import java.util.List;

// ret i32 0 或 ret void
public class IrReturn extends IrInstruction implements IrTerminator {

    public IrValue value; // 可以为 null (void return)

    public IrReturn(IrValue value) {
        super(IrType.getVoid()); // ret 指令本身不产生值
        this.value = value;
    }

    public IrReturn() { // void return
        super(IrType.getVoid());
        this.value = null;
    }

    @Override
    public List<String> irOutput() {
        String line = getLHS() + "ret ";
        if (value != null) {
            line += value.toString();
        } else {
            line += "void";
        }
        return List.of(line);
    }
}
