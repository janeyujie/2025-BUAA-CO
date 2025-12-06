package sysy.backend;

import java.util.ArrayList;
import java.util.List;

public class MipsFunc implements MipsNode {
    public String name;
    public List<MipsBasicBlock> blocks = new ArrayList<>();
    public int stackSize; // 需要计算栈帧大小

    public MipsFunc(String name) {
        this.name = name;
    }

    @Override
    public List<String> mipsOutput() {
        List<String> lines = new ArrayList<>();
        lines.add(name + ":");
        for (MipsBasicBlock b : blocks)
            lines.addAll(b.mipsOutput());
        return lines;
    }
}
