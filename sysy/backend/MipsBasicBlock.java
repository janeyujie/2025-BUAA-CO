package sysy.backend;

import sysy.backend.MipsInstruction.MipsInstruction;

import java.util.ArrayList;
import java.util.List;

public class MipsBasicBlock implements MipsNode{
    public String label;
    public List<MipsInstruction> instructions = new ArrayList<>();

    public MipsBasicBlock(String label) { this.label = label; }

    public void addInstr(MipsInstruction instr) { instructions.add(instr); }

    @Override
    public List<String> mipsOutput() {
        List<String> lines = new ArrayList<>();
        if (!label.isEmpty())
            lines.add(label + ":");
        for (MipsInstruction i : instructions) lines.addAll(i.mipsOutput());
        return lines;
    }
}
