package sysy.backend;

// 用于记录 LLVM IR 的 Value (变量/常量) 在 MIPS 中的位置
public class MipsSymbol {
    public boolean isGlobal; // 可能还要考虑const(全局/局部)/static(局部)/var(全局/局部)
    public int offset; // 相对于 $fp (如果是局部) 或 $gp (如果是全局)
    public String reg; // 如果分配了寄存器 (本次主要用栈，这个字段可能暂时不用)

    public MipsSymbol(boolean isGlobal, int offset) {
        this.isGlobal = isGlobal;
        this.offset = offset;
    }
}
