package sysy.middle.ir;

import java.util.List;

//  %a.ptr = alloca i32
public class IrAlloca extends IrInstruction {

    private IrType allocatedType; // 要分配的类型 (e.g., i32)

    public IrAlloca(IrType allocatedType) {
        // 'alloca' 指令本身产生一个指针
        super(IrType.getPointer(allocatedType));
        this.allocatedType = allocatedType;
    }

    @Override
    public List<String> irOutput() {
        //  %a.ptr = alloca i32
        String line = getLHS() + "alloca " + allocatedType.toString();
        return List.of(line);
    }
}
