package sysy.middle.ir;

import java.util.ArrayList;
import java.util.List;

public class IrPhi extends IrInstruction {
    private List<IrValue> values = new ArrayList<>();
    private List<IrBasicBlock> blocks = new ArrayList<>();

    public IrPhi(IrType type) {
        super(type); // e.g., i1
    }

    // 如果我从 'block_i' 来, 那么我的值就是 'value_i'
    public void addIncoming(IrValue value, IrBasicBlock block) {
        values.add(value);
        blocks.add(block);
    }

    @Override
    public List<String> irOutput() {
        String line = getLHS() + "phi " + irType.toString();
        for (int i = 0; i < values.size(); i++) {
            line += (i == 0 ? " " : ", ") +
                    "[" + values.get(i).getNameOrConst() + ", " + blocks.get(i).getNameOrConst() + "]";
        }
        return List.of(line);
    }
}
