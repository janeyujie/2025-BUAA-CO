package sysy.backend;

import java.util.ArrayList;
import java.util.List;

public class MipsModule implements MipsNode {
    // .data段
    public List<MipsData> dataList = new ArrayList<>();
    // .text段
    public List<MipsFunc> funcList = new ArrayList<>();

    @Override
    public List<String> mipsOutput() {
        List<String> lines = new ArrayList<>();
        lines.add(".data");
        for (MipsData d : dataList)
            lines.addAll(d.mipsOutput());

        lines.add("");
        lines.add(".text");
        // 全局初始化代码可能需要特殊处理，或者放在 main 函数开头
        lines.add("jal main"); // 入口
        // 这个部分是最后结尾？
        lines.add("li $v0, 10");
        lines.add("syscall");
        lines.add("");

        for (MipsFunc f : funcList)
            lines.addAll(f.mipsOutput());
        return lines;
    }
}
