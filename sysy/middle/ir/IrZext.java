package sysy.middle.ir;

import java.util.List;

public class IrZext extends IrInstruction{
    private IrValue valueToExt;
    public IrZext(IrValue valueToExt, IrType targetType) {
        super(targetType); // 结果是 i32
        this.valueToExt = valueToExt;
    }
    @Override
    public List<String> irOutput() {
        String line = getLHS() + "zext " + valueToExt.toString() + " to " + irType.toString();
        return List.of(line);
    }
}
