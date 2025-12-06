package sysy.backend;

public class MipsReg {
    public static final String ZERO = "$zero";
    public static final String V0 = "$v0";
    public static final String A0 = "$a0", A1 = "$a1", A2 = "$a2", A3 = "$a3";
    public static final String GP = "$gp";
    public static final String SP = "$sp";
    public static final String FP = "$fp";
    public static final String RA = "$ra";

    // 临时寄存器，用于计算 $t0-$t9
    public static final String T0 = "$t0", T1 = "$t1", T2 = "$t2", T3 = "$t3", T4 = "$t4", T5 = "$t5", T6 = "$t6", T7 = "$t7", T8 = "$t8", T9 = "$t9";
}
