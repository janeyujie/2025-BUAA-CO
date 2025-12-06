package sysy.backend.MipsInstruction;

import java.util.List;

// 系统调用: syscall
public class MipsSyscall extends MipsInstruction {
    @Override
    public List<String> mipsOutput() {
        return List.of("syscall");
    }
}
