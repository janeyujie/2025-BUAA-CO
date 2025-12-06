package sysy.backend.MipsInstruction;

import java.util.List;
import java.util.Objects;

// 二元运算: add $t0, $t1, $t2
public class MipsBinary extends MipsInstruction {
    public String op, rd, rs, rt;

    public MipsBinary(String op, String rd, String rs, String rt) {
        this.op = op;
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }

    public MipsBinary(String op, String rs, String rt) {
        this.op = op;
        this.rs = rs;
        this.rt = rt;
    }

    public MipsBinary(String op, String rd) {
        this.op = op;
        this.rd = rd;
    }

    @Override
    public java.util.List<String> mipsOutput() {
        if (Objects.equals(op, "div")) return List.of(String.format("%s %s, %s", op, rs, rt));
        else if (Objects.equals(op, "mfhi")) return List.of(String.format("%s %s", op, rd));
        else if (Objects.equals(op, "mflo")) return List.of(String.format("%s %s", op, rd));
        else return List.of(String.format("%s %s, %s, %s", op, rd, rs, rt));
    }
}
