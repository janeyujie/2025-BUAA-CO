package sysy.backend.MipsInstruction;

import java.util.List;

// 加载立即数: li $t0, 100
public class MipsLi extends MipsInstruction {
    public String rd;
    public int imm;

    public MipsLi(String rd, int imm) {
        this.rd = rd;
        this.imm = imm;
    }

    @Override
    public List<String> mipsOutput() {
        return List.of(String.format("li %s, %d", rd, imm));
    }
}
