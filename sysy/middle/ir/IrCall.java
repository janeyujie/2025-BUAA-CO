package sysy.middle.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IrCall extends IrInstruction {

    private IrFunction functionToCall; // 指向我们正在调用的 IrFunction 对象
    private List<IrValue> arguments;

    public IrCall(IrFunction functionToCall, List<IrValue> arguments) {
        // call 指令的类型是它所调用函数的返回类型
        super(functionToCall.returnType);
        this.functionToCall = functionToCall;
        this.arguments = (arguments != null) ? arguments : new ArrayList<>();
    }

    public IrCall(IrFunction functionToCall) {
        this(functionToCall, null);
    }

    @Override
    public List<String> irOutput() {
        // 格式化参数: (i32 %a, i32 10)
        String argsStr = arguments.stream()
                .map(arg -> arg.toString()) // IrValue.toString() 应为 "i32 %name" 或 "i32 10"
                .collect(Collectors.joining(", "));


        String line = getLHS() + "call " + functionToCall.returnType.toString() + " " + functionToCall.name + "(" + argsStr + ")";

        // getLhs() 会自动处理:
        // - "  %t1 = " (如果 retType 是 i32)
        // - "  "     (如果 retType 是 void)

        return List.of(line);
    }
}
