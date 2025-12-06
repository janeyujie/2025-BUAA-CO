package sysy.backend.MipsInstruction;

import java.util.List;

// 访存: lw $t0, 4($sp)
public class MipsMem extends MipsInstruction {
    public String op, rt, base;
    public int offset;

    public MipsMem(String op, String rt, String base, int offset) {
        this.op = op;
        this.rt = rt;
        this.base = base;
        this.offset = offset;
    }

    @Override
    public java.util.List<String> mipsOutput() {
        return List.of(String.format("%s %s, %d(%s)", op, rt, offset, base));
    }
}
