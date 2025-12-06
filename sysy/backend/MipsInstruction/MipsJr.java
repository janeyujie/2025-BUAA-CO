package sysy.backend.MipsInstruction;

import java.util.List;

// 跳转寄存器: jr $ra
public class MipsJr extends MipsInstruction {
    public String rs;

    public MipsJr(String rs) {
        this.rs = rs;
    }

    @Override
    public List<String> mipsOutput() {
        return List.of(String.format("jr %s", rs));
    }
}
