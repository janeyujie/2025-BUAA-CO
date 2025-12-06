package sysy.frontend.symtable.symbol;

import sysy.middle.ir.type.IrType;

import java.util.List;

public class VarSymbol extends Symbol{
    public boolean isArray;
    public Integer constValue;
    public List<Integer> constArrayValues;
    public int dim; // 数组长度
    public int btype; // 0为int, 1为constInt, 2为staticInt
    public boolean isParam = false; // 区分数组和指针

    @Override
    public IrType getIrType() {
        if (this.isArray) {
            if (this.isParam) {
                // 是函数的参数 int a[]
                return IrType.getPointer(IrType.getInt32());
            } else {
                // 数组
                if (this.dim <= 0) {
                    throw new RuntimeException("数组 " + ident + " 缺少维度! " + this.dim);
                }
                return IrType.getArray(IrType.getInt32(), this.dim);
            }

        } else {
            return IrType.getInt32();
        }
    }
}
