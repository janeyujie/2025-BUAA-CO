package sysy.frontend.symtable.symbol;

import java.util.ArrayList;
import java.util.List;

public class FuncSymbol extends Symbol{
    public String returnType; // intFunc or voidFunc
    // 记录形参的type&len
    public final List<VarSymbol> params = new ArrayList<>();
}
