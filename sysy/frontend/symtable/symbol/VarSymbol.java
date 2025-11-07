package sysy.frontend.symtable.symbol;

public class VarSymbol extends Symbol{
    public boolean isArray;
    public Integer value;
    public int dim; // 数组长度
    public int btype; // 0为int, 1为constInt, 2为staticInt
}
