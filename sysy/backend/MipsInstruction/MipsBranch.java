package sysy.backend.MipsInstruction;

import java.util.List;

public class MipsBranch extends MipsInstruction {
    // 有条件跳转beq $t0, $zero, label_true
    public String op; // beq or bne
    public String rs, rt;
    public String label;

    public MipsBranch(String op, String rs, String rt, String label) {
        this.op = op;
        this.rs = rs;
        this.rt = rt;
        this.label = label;
    }

    @Override
    public List<String> mipsOutput() {
        return List.of(String.format("%s %s, %s, %s", op, rs, rt, label));
    }
}
