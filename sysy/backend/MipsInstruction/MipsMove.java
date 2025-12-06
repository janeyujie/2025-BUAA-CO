package sysy.backend.MipsInstruction;

import java.util.List;

// 移动: move $t0, $t1
public class MipsMove extends MipsInstruction {
    public String rd, rs;

    public MipsMove(String rd, String rs) {
        this.rd = rd;
        this.rs = rs;
    }

    @Override
    public List<String> mipsOutput() {
        return java.util.List.of(String.format("move %s, %s", rd, rs));
    }
}
