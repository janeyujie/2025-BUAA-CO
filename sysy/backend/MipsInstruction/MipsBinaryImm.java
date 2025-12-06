package sysy.backend.MipsInstruction;

import java.util.List;

// 二元运算立即数: addiu $t0, $t1, 100
public class MipsBinaryImm extends MipsInstruction {
    public String op, rt, rs;
    public int imm;

    public MipsBinaryImm(String op, String rt, String rs, int imm) {
        this.op = op;
        this.rt = rt;
        this.rs = rs;
        this.imm = imm;
    }

    @Override
    public java.util.List<String> mipsOutput() {
        return List.of(String.format("%s %s, %s, %d", op, rt, rs, imm));
    }
}
