package sysy.middle.ir;

import java.util.List;
import java.util.Objects;

// 'br' 是终结指令, 实现了 IrTerminator 接口
public class IrBranch extends IrInstruction implements IrTerminator {

    private IrValue condition; // 可以为 null
    private IrBasicBlock trueTarget;
    private IrBasicBlock falseTarget; // 可以为 null

    // 无条件跳转
    public IrBranch(IrBasicBlock target) {
        super(IrType.getVoid()); // br 不产生值
        this.condition = null;
        this.trueTarget = Objects.requireNonNull(target, "br target cannot be null");
        this.falseTarget = null;
    }

    // 有条件跳转
    public IrBranch(IrValue cond, IrBasicBlock trueTarget, IrBasicBlock falseTarget) {
        super(IrType.getVoid());
        this.condition = Objects.requireNonNull(cond, "br condition cannot be null");
        this.trueTarget = Objects.requireNonNull(trueTarget, "br trueTarget cannot be null");
        this.falseTarget = Objects.requireNonNull(falseTarget, "br falseTarget cannot be null");
    }

    @Override
    public List<String> irOutput() {
        String line;
        if (condition == null) {
            // 无条件: "  br label %if.merge"
            line = getLHS() + "br label " + trueTarget.getNameOrConst();
        } else {
            // 有条件: "  br i1 %cmp, label %if.then, label %if.else"
            line = getLHS() + "br " + condition.toString() + ", label " + trueTarget.getNameOrConst() + ", label " + falseTarget.getNameOrConst();
        }
        return List.of(line);
    }
}
