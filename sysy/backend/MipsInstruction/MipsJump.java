package sysy.backend.MipsInstruction;

import java.util.List;

// 跳转: jal label / j
public class MipsJump extends MipsInstruction {
    public String op, target;

    public MipsJump(String op, String target) {
        this.op = op;
        this.target = target;
    }

    @Override
    public List<String> mipsOutput() {
        return List.of(String.format("%s %s", op, target));
    }
}
