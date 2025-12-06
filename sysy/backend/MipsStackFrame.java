package sysy.backend;

import sysy.middle.ir.value.IrValue;

import java.util.HashMap;
import java.util.Map;

// 管理当前函数的栈帧
public class MipsStackFrame {
    // 记录 LLVM IrValue -> MipsSymbol 的映射
    private Map<IrValue, MipsSymbol> locationMap = new HashMap<>();
    private int currentOffset = 0; // 当前 $fp 偏移量

    // 分配局部变量空间 (往 + 方向增长，根据你的 2.2.4)
    public int alloc(IrValue value, int size) {
        // 按照你的设计，$fp 向上增长
        int offset = currentOffset;
        currentOffset += size;
        locationMap.put(value, new MipsSymbol(false, offset));
        return offset;
    }

    public MipsSymbol get(IrValue value) {
        return locationMap.get(value);
    }

    public int getFrameSize() { return currentOffset; }
}
