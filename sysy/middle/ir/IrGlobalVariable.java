package sysy.middle.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// 代表一个 LLVM 全局变量, 既是IrValue又是IrNode
public class IrGlobalVariable extends IrValue implements IrNode {

    private IrConstant scalarInitializer;
    private List<IrConstant> arrayInitializer;

    private IrType baseType;
    private boolean isConstant; // LLVM 的 'constant' (SysY const) vs 'global' (SysY var)

    public IrGlobalVariable(String name, IrType type, IrConstant initializer, boolean isConstant) {
        // 全局变量的值是一个指向其内容的指针
        super(IrType.getPointer(type));
        this.baseType = type;
        this.name = "@" + name; // 全局变量名以 @ 开头
        this.scalarInitializer = initializer;
        this.arrayInitializer = null;
        this.isConstant = isConstant; // 是否为不可变的常量
    }

    public IrGlobalVariable(String name, IrType type, List<IrConstant> inits, boolean isConstant) {
        super(IrType.getPointer(type));
        this.name = ("@" + name);
        this.baseType = type;
        this.isConstant = isConstant;
        this.scalarInitializer = null;
        this.arrayInitializer = inits; // 存储列表
    }


    @Override
    public String getOperandString() { return irType.toString() + " " + this.name; } // e.g., "i32* @g_var"

    @Override
    public String getNameOrConst() { return this.name; }

    @Override
    public List<String> irOutput() {
        // 决定是 global (可变) 还是 constant (不可变)
        String linkage = isConstant ? "constant" : "global";
        String initStr;

        // 获取指针指向的基础类型
        IrType baseType = ((IrPointerType)this.irType).baseType;

        if (arrayInitializer != null) {
            // 将 List<IrConstant> 转换成 "i32 1", "i32 2", "i32 3"
            String elemType = ((IrArrayType) baseType).baseType.toString(); // "i32"
            int totalDim = ((IrArrayType) baseType).size;
            int numInits = arrayInitializer.size();

            List<String> paddedInits = new ArrayList<>();

            // 添加显式初始值
            for (IrConstant init : arrayInitializer) {
                paddedInits.add(elemType + " " + init.getNameOrConst()); // "i32 1", "i32 2"
            }
            // 添加未初始化部分的 0 填充
            for (int i = numInits; i < totalDim; i++) {
                paddedInits.add(elemType + " 0"); // "i32 0", "i32 0", ...
            }

            initStr = "[" + String.join(", ", paddedInits) + "]";
        } else if (scalarInitializer != null) {
            initStr = scalarInitializer.getNameOrConst();
        } else {
            initStr = "zeroinitializer";
        }
        String line = this.name + " = dso_local " + linkage + " " + baseType.toString() + " " + initStr;
        return List.of(line);
    }
}
