package sysy.middle.ir.instruction;

import sysy.middle.ir.type.IrType;
import sysy.middle.ir.value.IrValue;

import java.util.List;

public class IrStore extends IrInstruction {

    public IrValue value; // 要存储的值
    public IrValue pointer; // 存储到的地址

    public IrStore(IrValue value, IrValue pointer) {
        super(IrType.getVoid()); // Store 不产生值
        this.value = value;
        this.pointer = pointer;
    }

    @Override
    public List<String> irOutput() {
        String line = getLHS() + "store " + value.toString() + ", " + pointer.toString();
        return List.of(line);
    }
}
