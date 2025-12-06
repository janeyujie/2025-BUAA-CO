package sysy.backend.MipsInstruction;

import java.util.List;

// 加载地址: la $a0, label
public class MipsLa extends MipsInstruction {
    public String rd;
    public String label;

    public MipsLa(String rd, String label) {
        this.rd = rd;
        this.label = label;
    }

    @Override
    public List<String> mipsOutput() {
        return List.of(String.format("la %s, %s", rd, label));
    }
}
