package sysy.frontend.visitor;

import sysy.middle.ir.IrValue;

import java.util.List;

public class VisitResult {
    public boolean ifArray;
    public boolean ifConst;
    public String returnType;
    public int number;
    public List<Integer> numbers;

    public IrValue value;

    public VisitResult(){}

    public VisitResult(IrValue value) {
        this.value = value;
    }
}
