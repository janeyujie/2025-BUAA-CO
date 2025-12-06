package sysy.middle.ir;

import sysy.middle.ir.instruction.IrBasicBlock;
import sysy.middle.ir.type.IrType;
import sysy.middle.ir.value.IrParamValue;
import sysy.middle.ir.value.IrValue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IrFunction extends IrValue implements IrNode { // 函数也是一个 IrValue

    public List<IrBasicBlock> basicBlocks = new ArrayList<>();
    public IrModule parent;

    // 函数的参数列表
    public List<IrParamValue> parameters = new ArrayList<>();

    // 函数的返回类型
    public IrType returnType;

    // 关键：区分 'define' (有函数体) 和 'declare' (无函数体)
    public boolean isDeclaration;

    public IrFunction(String name, IrType returnType, IrModule parent, boolean isDeclaration) {
        super(returnType); // IrValue 需要一个类型
        this.name = "@" + name; // LLVM 全局标识符以 @ 开头
        this.returnType = returnType;
        this.parent = parent;
        this.isDeclaration = isDeclaration;
    }

    public void addParam(IrParamValue param) {
        this.parameters.add(param);
    }

    // 一个方便的工厂方法, 自动设置 parent
    public IrBasicBlock createBasicBlock(String name) {
        IrBasicBlock block = new IrBasicBlock(name, this);
        this.basicBlocks.add(block);
        return block;
    }

    // 生成函数签名字符串 "i32 @main(i32 %a, i32 %b)"
    private String getSignature() {
        String paramStr = parameters.stream()
                .map(p -> p.toString()) // IrParamValue.toString() 应为 "i32 %name"
                .collect(Collectors.joining(", "));

        return returnType.toString() + " " + this.name + "(" + paramStr + ")";
    }

    @Override
    public List<String> irOutput() {
        List<String> output = new ArrayList<>();

        if (isDeclaration) {
            // 如果是声明: "declare i32 @getint()"
            output.add("declare " + getSignature());
        } else {
            // 如果是定义: "define i32 @main(...) {" ... "}"
            output.add("define dso_local " + getSignature() + " {");

            // 递归下降：调用所有 BasicBlock 的 irOutput
            for (IrBasicBlock block : basicBlocks) {
                // IrBasicBlock.irOutput() 会返回带缩进的指令列表
                output.addAll(block.irOutput());
            }

            output.add("}");
        }

        return output;
    }

    @Override
    public String getOperandString() {
        return irType.toString() + " " + this.name;
    }

    @Override
    public String getNameOrConst() {
        return this.name;
    }
}
