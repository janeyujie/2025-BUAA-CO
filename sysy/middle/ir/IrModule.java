package sysy.middle.ir;

import sysy.middle.ir.type.IrType;
import sysy.middle.ir.value.IrGlobalVariable;
import sysy.middle.ir.value.IrParamValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IrModule implements IrNode{

    public List<IrGlobalVariable> globalVariables = new ArrayList<>();
    public List<IrFunction> functions = new ArrayList<>();

    // 字符串字面量也需要存储为全局常量
    // 我们可以用一个 Map 来重用它们
    private Map<String, IrGlobalVariable> stringLiterals = new HashMap<>();

    public IrModule() {
        // 添加 IO 函数声明
        addIoDeclarations();
    }

    // 添加IO函数声明
    private void addIoDeclarations() {
        // i32 @getint()
        functions.add(new IrFunction("getint", IrType.getInt32(), this, true /*isDeclaration*/));

        // void @putint(i32)
        IrFunction putint = new IrFunction("putint", IrType.getVoid(), this, true);
        putint.addParam(new IrParamValue(IrType.getInt32(), "%val")); // 参数名不重要, 类型重要
        functions.add(putint);

        // void @putch(i32)
        IrFunction putch = new IrFunction("putch", IrType.getVoid(), this, true);
        putch.addParam(new IrParamValue(IrType.getInt32(), "%val"));
        functions.add(putch);
    }

    public IrFunction createFunction(String name, String sysyReturnType) {
        IrType returnType = sysyReturnType.equals("int") ? IrType.getInt32() : IrType.getVoid();
        IrFunction func = new IrFunction(name, returnType, this, false /*isDeclaration*/);
        this.functions.add(func);
        return func;
    }

    public IrFunction getFunction(String name) {
        String searchName = "@" + name;
        for (IrFunction func : this.functions) {
            if (func.name.equals(searchName)) {
                return func;
            }
        }
        throw new RuntimeException("Cannot find function " + name);
    }

    public void addFunction(IrFunction func) {
        this.functions.add(func);
    }

    public void addGlobalVariable(IrGlobalVariable gv) {
        this.globalVariables.add(gv);
    }

    @Override
    public List<String> irOutput() {
        List<String> output = new ArrayList<>();

        // 打印函数声明
//        for (IrFunction func : functions) {
//            if (func.isDeclaration)
//                output.addAll(func.irOutput());
//        }
        output.add("declare i32 @getint()");
        output.add("declare void @putint(i32)");
        output.add("declare void @putch(i32)");
        output.add("");

        // 打印全局变量
        for (IrGlobalVariable gv : globalVariables) {
            output.addAll(gv.irOutput());
        }
        if (!globalVariables.isEmpty()) output.add("");

        // 打印字符串字面量
        for (IrGlobalVariable str : stringLiterals.values()) {
            output.addAll(str.irOutput());
        }
        if (!stringLiterals.isEmpty()) output.add("");

        // 打印函数定义
        for (IrFunction func : functions) {
            if (!func.isDeclaration) {
                output.addAll(func.irOutput());
                output.add("");
            }
        }

        return output;
    }
}
