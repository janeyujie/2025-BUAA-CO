package sysy.middle.ir;

import java.util.List;

// --- IrStore (内存写入, 无返回值) ---
// e.g., "  store i32 %t1, i32* %a.ptr"
public class IrStore extends IrInstruction {

    private IrValue value; // 要存储的值 (e.g., %t1)
    private IrValue pointer; // 存储到的地址 (e.g., %a.ptr, 必须是指针类型)

    public IrStore(IrValue value, IrValue pointer) {
        super(IrType.getVoid()); // Store 不产生值
        this.value = value;
        this.pointer = pointer;
    }

    @Override
    public List<String> irOutput() {
        // getLhs() -> "  " (因为 type 是 void)
        String line = getLHS() + "store " + value.toString() + ", " + pointer.toString();
        return List.of(line);
    }
}
