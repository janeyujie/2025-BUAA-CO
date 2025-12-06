package sysy.backend;

import java.util.List;

public class MipsData implements MipsNode {
    public String label;
    public String content; // 需要打印的字符串
    public boolean ifString;

    public MipsData(String label, String content, boolean ifString) {
        this.label = label;
        if (!ifString) {
            this.content = content;
        } else {
            this.content = ".asciiz \"" + content.replace("\n", "\\n") + "\"";
        }
    }

    @Override
    public List<String> mipsOutput() {
        // 做了转义字符的处理
        return List.of(label + ": " + content);
    }
}
