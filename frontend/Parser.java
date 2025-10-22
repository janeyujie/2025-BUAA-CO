package frontend;

import ast.*;
import ast.Number;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    // 控制是否输出语法成分的开关
    public final static boolean ENABLE_PARSER_OUTPUT = true;

    private final List<Token> tokens;
    private int currentPos;

    // 用于存储正确的语法分析输出
    private final StringBuilder outputBuilder = new StringBuilder();
    // 用于存储错误信息
    private final List<SyntaxError> errors = new ArrayList<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public String getOutput() {
        return outputBuilder.toString();
    }
    public List<SyntaxError> getErrors() {
        return errors;
    }

    // 查看相对于当前位置的Token
    private Token peek(int offset) {
        int pos = currentPos + offset;
        if (pos >= tokens.size()) {
            return tokens.get(tokens.size()-1); // 返回EOF token？
        }
        return tokens.get(pos);
    }
    private Token currentToken() {
        return peek(0);
    }
    // 消费一个Token, 并将Token本身加入输出
    private void consumeToken() {
        if (ENABLE_PARSER_OUTPUT && currentToken().getType() != TokenType.EOFSY) {
            outputBuilder.append(currentToken().getType().name())
                    .append(" ")
                    .append(currentToken().getContent())
                    .append("\n");
        }
        currentPos++;
    }

    // 期望一个特定类型的Token，如果匹配则消费，否则记录错误
    private void expect(TokenType expected, String errorCode, Boolean ifPrint) {
        if (currentToken().getType() == expected) {
            consumeToken();
        } else {
            // 错误：缺失符号，错误行号应为前一个符号的行号
            int errorLine = (currentPos > 0) ? tokens.get(currentPos - 1).getLineno() : 1;
            if (ifPrint)
                errors.add(new SyntaxError(errorLine, errorCode));
            //currentPos++;
        }
    }

    private void printComponent(String c) {
        if (ENABLE_PARSER_OUTPUT) {
            outputBuilder.append("<").append(c).append(">\n");
        }
    }

    public Node parse() {
        return parseCompUnit();
    }

    // CompUnit -> {Decl} {FuncDef} MainFuncDef
    // 声明:  const int ... / int ident [ or ; or =
    // 函数: int ident (
    private CompUnit parseCompUnit() {

        CompUnit node = new CompUnit();
        node.topLevelNodes = new ArrayList<>();

        while (currentToken().getType() != TokenType.EOFSY) {


            if (peek(2).getType() != TokenType.LPARENT) { // 声明

                node.topLevelNodes.add(parseDecl());
            } else { //函数定义
                if (peek(0).getType() == TokenType.INTTK && peek(1).getType() == TokenType.MAINTK)
                    break;
                node.topLevelNodes.add(parseFuncDef());
            }

        }
        node.topLevelNodes.add(parseMainFuncDef());
        printComponent("CompUnit");
        return node;
    }

    // Decl -> ConstDecl | VarDecl
    // 区别就是有没有const
    private DeclNode parseDecl() {
        if (currentToken().getType() == TokenType.CONSTTK) {
            return parseConstDecl();
        } else {
            return parseVarDecl();
        }
    }

    // ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
    private ConstDecl parseConstDecl() {

        ConstDecl node = new ConstDecl();
        node.constDefs = new ArrayList<>();

        // 匹配 'const'
        expect(TokenType.CONSTTK, "const_error", false);
        parseBType(); //btype只能是int

        node.constDefs.add(parseConstDef());
        while(currentToken().getType() == TokenType.COMMA) {
            consumeToken(); // 跳过','
            node.constDefs.add(parseConstDef());
        }
        // 匹配 ';'
        expect(TokenType.SEMICN, "i", true);
        printComponent("ConstDecl");
        return node;
    }

    // VarDecl -> [static] BType VarDef { ',' VarDef } ';'
    private VarDecl parseVarDecl() {

        VarDecl node = new VarDecl();
        node.varDefs = new ArrayList<>();

        if (currentToken().getType() == TokenType.STATICTK) {
            // 考虑一下static的情况
            consumeToken();
            node.isStatic = true;
        }
        parseBType();

        node.varDefs.add(parseVarDef());
        while (currentToken().getType() == TokenType.COMMA) {
            consumeToken();
            node.varDefs.add(parseVarDef());
        }
        expect(TokenType.SEMICN, "i", true);
        printComponent("VarDecl");
        return node;
    }

    // BType -> 'int'
    private void parseBType() {
        expect(TokenType.INTTK, "int_error", false);
    }

    // ConstDef -> Ident [ '[' ConstExp ']' ] '=' ConstInitval
    private ConstDef parseConstDef() {

        ConstDef node = new ConstDef();
        node.arrayIndexes = new ArrayList<>();
        node.identName = currentToken().getContent();
        // 匹配是否为标识符
        expect(TokenType.IDENFR, "ident_error", false);

        if (currentToken().getType() == TokenType.LBRACK) {
            consumeToken();
            node.arrayIndexes.add(parseConstExp());
            expect(TokenType.RBRACK, "k", true);
        }

        expect(TokenType.ASSIGN, "assign_error", false);
        node.initialValue = parseConstInitVal();
        printComponent("ConstDef");
        return node;
    }

    // VarDef -> Ident [ '[' ConstExp ']' ] [ '=' Initval ]
    private VarDef parseVarDef() {

        VarDef node = new VarDef();
        node.arrayIndexes = new ArrayList<>();
        node.identName = currentToken().getContent();
        expect(TokenType.IDENFR, "ident_error", false);

        if (currentToken().getType() == TokenType.LBRACK) {
            consumeToken();
            node.arrayIndexes.add(parseConstExp());
            expect(TokenType.RBRACK, "k", true);
        }

        if (currentToken().getType() == TokenType.ASSIGN) {
            consumeToken();
            node.initialValue = parseInitVal();
        }
        printComponent("VarDef");
        return node;
    }

    // ConstInitval -> ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}'
    private InitVal parseConstInitVal() {

        InitVal node = new InitVal();
        if (currentToken().getType() == TokenType.LBRACE) {
            // 后一种情况, 说明是对数组的初始化
            consumeToken();
            node.isArray = true;
            node.arrayExprs = new ArrayList<>();
            // 有可能初始化为空(下一个token为rbrace)，也可能包含i个ConstExp
            if (currentToken().getType() != TokenType.RBRACE) {
                node.arrayExprs.add(parseConstExp());
                while(currentToken().getType() == TokenType.COMMA) {
                    consumeToken();
                    node.arrayExprs.add(parseConstExp());
                }
            }
            // 不管有没有constexp都要匹配右大括号
            expect(TokenType.RBRACE, "rbrace_error", false);
        } else {
            node.isArray = false;
            node.singleExpr = parseConstExp();
        }
        printComponent("ConstInitVal");
        return node;
    }

    // Initval -> Exp | '{' [ Exp { ',' Exp } ] '}'
    private InitVal parseInitVal() {

        InitVal node = new InitVal();
        if (currentToken().getType() == TokenType.LBRACE) {
            consumeToken();
            node.isArray = true;
            node.arrayExprs = new ArrayList<>();
            if (currentToken().getType() != TokenType.RBRACE) {
                node.arrayExprs.add(parseExp());
                while(currentToken().getType() == TokenType.COMMA) {
                    consumeToken();
                    node.arrayExprs.add(parseExp());
                }
            }
            expect(TokenType.RBRACE, "rbrace_error", false);
        } else {
            node.isArray = false;
            node.singleExpr = parseExp();
        }
        printComponent("InitVal");
        return node;
    }

    // FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
    private FuncDef parseFuncDef() {

        FuncDef node = new FuncDef();
        node.returnType = parseFuncType();
        node.funcName = currentToken().getContent();
        expect(TokenType.IDENFR, "ident_error", false);
        expect(TokenType.LPARENT, "lparent_error", false);
        node.params = new ArrayList<>();
        // FuncFParams -> FuncFParam -> BType -> INTTK 开头
        if (currentToken().getType() != TokenType.RPARENT && currentToken().getType() == TokenType.INTTK) {
            node.params.addAll(parseFuncFParams());
        }
        expect(TokenType.RPARENT, "j", true);

        node.funcBody = parseBlock();
        printComponent("FuncDef");
        return node;
    }

    // MainFuncDef -> 'int' 'main' '(' ')' Block
    private FuncDef parseMainFuncDef() {

        FuncDef node = new FuncDef();
        node.returnType = "int";
        expect(TokenType.INTTK, "int_error", false);
        expect(TokenType.MAINTK, "main_error", false);
        node.funcName = "main";
        expect(TokenType.LPARENT, "lparent_error", false);
        expect(TokenType.RPARENT, "j", true);
        node.funcBody = parseBlock();
        printComponent("MainFuncDef");
        return node;
    }

    // FuncType -> 'void' | 'int'
    private String parseFuncType() {
        String type;
        if (currentToken().getType() == TokenType.VOIDTK) {
            consumeToken();
            type = "void";
        } else {
            expect(TokenType.INTTK, "func_type_error", false);
            type = "int";
        }
        printComponent("FuncType");
        return type;
    }

    // FuncFParams -> FuncFParam { ',' FuncFParam }
    private List<FuncFParam> parseFuncFParams() {

        List<FuncFParam> params = new ArrayList<>();
        params.add(parseFuncFParam());
        while(currentToken().getType() == TokenType.COMMA) {
            consumeToken();
            params.add(parseFuncFParam());
        }
        printComponent("FuncFParams");
        return params;
    }

    // FuncFParam -> BType Ident ['[' ']']
    private FuncFParam parseFuncFParam() {

        FuncFParam param = new FuncFParam();
        parseBType();
        param.paramName = currentToken().getContent();
        expect(TokenType.IDENFR, "ident_error", false);
        if (currentToken().getType() == TokenType.LBRACK) {
            consumeToken();
            param.isArray = true;
            expect(TokenType.RBRACK, "k", true);
        } else {
            param.isArray = false;
        }
        printComponent("FuncFParam");
        return param;
    }

    // Block -> '{' { BlockItem } '}'
    private Block parseBlock() {

        Block node = new Block();
        node.items = new ArrayList<>();
        expect(TokenType.LBRACE, "lbrace_error", false);
        while (currentToken().getType() != TokenType.RBRACE
        && currentToken().getType() != TokenType.EOFSY) {
            // BlockItem -> Decl | Stmt
            // Decl -> const int | int
            // Stmt -> LVal | ....
            if (currentToken().getType() == TokenType.CONSTTK || currentToken().getType() == TokenType.INTTK || currentToken().getType() == TokenType.STATICTK) {
                node.items.add(parseDecl());
            } else {
                node.items.add(parseStmt());
            }
        }
        expect(TokenType.RBRACE, "rbrace_error", false);
        printComponent("Block");
        return node;
    }

    // Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    //| [Exp] ';' // 有无Exp两种情况；printf函数调用
    //| Block
    //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省，1种情况 2. ForStmt与
    //Cond中缺省一个，3种情况 3. ForStmt与Cond中缺省两个，3种情况 4. ForStmt与Cond全部缺省，1种情况
    //| 'break' ';'
    //| 'continue' ';'
    //| 'return' [Exp] ';' // 1.有Exp 2.无Exp
    //| 'printf''('StringConst {','Exp}')'';' // 1.有Exp 2.无Exp
    private StmtNode parseStmt() {

        StmtNode node;
        switch (currentToken().getType()) {
            case LBRACE:
                node = parseBlock();
                break;
            case IFTK:
                consumeToken();
                IfStmt ifnode = new IfStmt();
                expect(TokenType.LPARENT, "lparent_error", false);
                ifnode.condition = parseCond();
                expect(TokenType.RPARENT, "j", true);
                ifnode.thenStmt = parseStmt();
                if (currentToken().getType() == TokenType.ELSETK) {
                    consumeToken();
                    ifnode.elseStmt = parseStmt();
                }
                node = ifnode;
                break;
            case FORTK:
                // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                //  ForStmt -> LVal '=' Exp { ',' LVal '=' Exp }
                consumeToken();
                ForStmt fornode = new ForStmt();
                fornode.initStmts = new ArrayList<>();
                fornode.updateStmts = new ArrayList<>();

                expect(TokenType.LPARENT, "lparent_error", false);
                if (currentToken().getType() != TokenType.SEMICN) {
                    fornode.initStmts.addAll(parseForStmt());
                }
                expect(TokenType.SEMICN, "i", true);
                if (currentToken().getType() != TokenType.SEMICN) {
                    fornode.condition = parseCond();
                }
                expect(TokenType.SEMICN, "i", true);
                if (currentToken().getType() != TokenType.RPARENT) {
                    fornode.updateStmts.addAll(parseForStmt());
                }
                expect(TokenType.RPARENT, "j", true);
                fornode.body = parseStmt();
                node = fornode;
                break;
            case BREAKTK:
                consumeToken();
                expect(TokenType.SEMICN, "i", true);
                node = new BreakStmt();
                break;
            case CONTINUETK:
                consumeToken();
                expect(TokenType.SEMICN, "i", true);
                node = new ContinueStmt();
                break;
            case RETURNTK:
                consumeToken();
                ReturnStmt returnnode = new ReturnStmt();
                if (currentToken().getType() != TokenType.SEMICN) {
                    returnnode.returnValue = parseExp();
                }
                expect(TokenType.SEMICN, "i", true);
                node = returnnode;
                break;
            case PRINTFTK:
                // 'printf''('StringConst {','Exp}')'';'
                consumeToken();
                PrintfStmt printfnode = new PrintfStmt();
                printfnode.args = new ArrayList<>();
                expect(TokenType.LPARENT, "lparent_error", false);
                if (currentToken().getType() == TokenType.STRCON) {
                    printfnode.formatString = currentToken().getContent();
                    consumeToken();
                } else {
                    // 报告错误，printf第一个参数必须是字符串常量
                }
                while (currentToken().getType() == TokenType.COMMA) {
                    consumeToken();
                    printfnode.args.add(parseExp());
                }
                expect(TokenType.RPARENT, "j", true);
                expect(TokenType.SEMICN, "i", true);
                node = printfnode;
                break;
            case SEMICN: // 空语句 ‘;’
                consumeToken();
                node = new ExprStmt(); // 返回一个空的expression
                break;
            default:
                // 可能是 LVal = Exp; 或 Exp;
                // LVal → Ident ['[' Exp ']']
                // Exp也可以是ident开头的 还包含了 ident(FuncRParam) 函数调用
                // 先用预读看看当前是不是一个赋值表达式
                if (isAssignStmt()) {
                    //consumeToken();
                    AssignStmt assignnode = new AssignStmt();
                    assignnode.lVal = (LVal) parseLVal();
                    expect(TokenType.ASSIGN, "assign_error", false);
                    assignnode.rValue = parseExp();
                    expect(TokenType.SEMICN, "i", true);
                    node = assignnode;
                } else {
                    ExprStmt exprnode = new ExprStmt();
                    exprnode.expr = parseExp();
                    expect(TokenType.SEMICN, "i", true);
                    node = exprnode;
                }
                break;
        }
        printComponent("Stmt");
        return node;
    }

    private boolean isAssignStmt() {
        // LVal 必须是标识符开头的
        // 可能是 LVal = Exp; 或 Exp;
        if (peek(0).getType() != TokenType.IDENFR) {
            return false;
        }
        // 模拟向后扫描，计数偏移
        int offset = 1;
        // 不是数组的情况
        if (peek(offset).getType() == TokenType.ASSIGN) {
            return true;
        }

        // 跳过'['
        if (peek(offset).getType() == TokenType.LBRACK) {
            offset++;

            while(peek(offset).getType() != TokenType.RBRACK
            && peek(offset).getType() != TokenType.EOFSY) {
                if (peek(offset).getType() == TokenType.ASSIGN){
                    return true;
                }
                offset++;
            }
            if (peek(offset).getType() != TokenType.RBRACK) {
                return false;
            } else if (peek(offset).getType() == TokenType.EOFSY) {
                return false;
            }
            offset++; //跳过']'
        }

        return peek(offset).getType() == TokenType.ASSIGN;
    }

    private AssignStmt parseAssign() {
        AssignStmt assignnode = new AssignStmt();
        assignnode.lVal = (LVal) parseLVal();
        expect(TokenType.ASSIGN, "assign_error", false);
        assignnode.rValue = parseExp();
        return assignnode;
    }

    private List<AssignStmt> parseForStmt() {
        //  ForStmt -> LVal '=' Exp { ',' LVal '=' Exp }
        List<AssignStmt> assigns = new ArrayList<>();
        assigns.add(parseAssign());
        while(currentToken().getType() == TokenType.COMMA) {
            consumeToken();
            assigns.add(parseAssign());
        }
        printComponent("ForStmt");
        return assigns;
    }


    // 表达式解析
    // ConstExp -> AddExp
    private ExprNode parseConstExp () {
        ExprNode node = parseAddExp();
        printComponent("ConstExp");
        return node;
    }

    // Exp -> AddExp
    private ExprNode parseExp() {
        ExprNode node = parseAddExp();
        printComponent("Exp");
        return node;
    }

    // Cond -> LOrExp
    private ExprNode parseCond() {
        ExprNode node = parseLOrExp();
        printComponent("Cond");
        return node;
    }
    // 优先级逻辑: LOrExp -> LAndExp -> EqExp -> RelExp -> AddExp -> MulExp -> UnaryExp -> PrimaryExp
    // LOrExp → LAndExp | LOrExp '||' LAndExp 需要消除左递归----
    // LOrExp → LAndExp LOrExp'
    // LOrExp' → '||' LAndExp LOrExp' | ε
    private ExprNode parseLOrExp() {
        ExprNode left = parseLAndExp();
        while (currentToken().getType() == TokenType.OR) {
            printComponent("LOrExp");
            consumeToken();
            ExprNode right = parseLAndExp();
            BinaryOpExp node = new BinaryOpExp();
            node.op = Operator.OR;
            node.left = left;
            node.right = right;
            left = node;
        }
        printComponent("LOrExp");
        return left;
    }

    // LAndExp → EqExp | LAndExp '&&' EqExp
    // LAndExp -> EqExp LAndExp'
    // LAndExp' -> '&&' EqExp LAndExp' | null
    private ExprNode parseLAndExp() {
        ExprNode left = parseEqExp();
        while (currentToken().getType() == TokenType.AND) {
            printComponent("LAndExp");
            consumeToken();
            ExprNode right = parseEqExp();
            BinaryOpExp node = new BinaryOpExp();
            node.op = Operator.AND;
            node.left = left;
            node.right = right;
            left = node;
        }
        printComponent("LAndExp");
        return left;
    }

    // EqExp → RelExp | EqExp ('==' | '!=') RelExp // 1.RelExp 2.== 3.!= 均需覆盖
    private ExprNode parseEqExp() {
        ExprNode left = parseRelExp();
        while(currentToken().getType() == TokenType.EQL ||
        currentToken().getType() == TokenType.NEQ) {
            printComponent("EqExp");
            TokenType type = currentToken().getType();
            consumeToken();
            ExprNode right = parseRelExp();
            BinaryOpExp node = new BinaryOpExp();
            node.op = (type == TokenType.EQL) ? Operator.EQ : Operator.NE;
            node.left = left;
            node.right = right;
            left = node;
        }
        printComponent("EqExp");
        return left;
    }

    private boolean isRelOp(TokenType type) {
        return type == TokenType.LSS || type == TokenType.LEQ ||
                type == TokenType.GRE || type == TokenType.GEQ;
    }
    // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    // 1.AddExp 2.< 3.> 4.<= 5.>= 均需覆盖
    private ExprNode parseRelExp() {
        ExprNode left = parseAddExp();
        while(isRelOp(currentToken().getType())) {
            printComponent("RelExp");
            TokenType type = currentToken().getType();
            consumeToken();
            ExprNode right = parseAddExp();
            BinaryOpExp node = new BinaryOpExp();
            switch (type) {
                case LSS: node.op = Operator.LT; break;
                case LEQ: node.op = Operator.LE; break;
                case GRE: node.op = Operator.GT; break;
                case GEQ: node.op = Operator.GE; break;
            }
            node.left = left;
            node.right = right;
            left = node;
        }
        printComponent("RelExp");
        return left;
    }

    //  AddExp → MulExp | AddExp ('+' | '−') MulExp
    // 1.MulExp 2.+ 需覆盖 3.- 需覆盖
    private ExprNode parseAddExp() {
        ExprNode left = parseMulExp();
        while(currentToken().getType() == TokenType.PLUS ||
        currentToken().getType() == TokenType.MINU) {
            printComponent("AddExp");
            TokenType type = currentToken().getType();
            consumeToken();
            ExprNode right = parseMulExp();
            BinaryOpExp node = new BinaryOpExp();
            node.op = (type == TokenType.PLUS) ? Operator.ADD : Operator.SUB;
            node.left = left;
            node.right = right;
            left = node;
        }
        printComponent("AddExp");
        return left;
    }

    // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    // 1.UnaryExp 2.* 3./ 4.% 均需覆盖
    private ExprNode parseMulExp() {
        ExprNode left = parseUnaryExp();
        while (currentToken().getType() == TokenType.MULT || currentToken().getType() == TokenType.DIV || currentToken().getType() == TokenType.MOD) {
            printComponent("MulExp");
            TokenType type = currentToken().getType();
            consumeToken();
            ExprNode right = parseUnaryExp();
            BinaryOpExp node = new BinaryOpExp();
            switch (type) {
                case MULT: node.op = Operator.MUL; break;
                case DIV: node.op = Operator.DIV; break;
                case MOD: node.op = Operator.MOD; break;
            }
            node.left = left;
            node.right = right;
            left = node;
        }
        printComponent("MulExp");
        return left;
    }

    //  UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    // 3种情况均需覆盖,函数调用也需要覆盖FuncRParams的不同情况
    private ExprNode parseUnaryExp() {
        ExprNode exp;
        // UnaryOp UnaryExp 的情况
        if (currentToken().getType() == TokenType.PLUS
                || currentToken().getType() == TokenType.MINU
                || currentToken().getType() == TokenType.NOT) {
            UnaryExp node = new UnaryExp();
            TokenType type = currentToken().getType();
            consumeToken();
            switch (type) {
                case PLUS: node.op = Operator.POS; break;
                case MINU: node.op = Operator.NEG; break;
                case NOT:  node.op = Operator.NOT; break;
            }
            printComponent("UnaryOp");
            node.operand = parseUnaryExp(); // 递归调用
            exp = node;
        } else if (currentToken().getType() == TokenType.IDENFR && peek(1).getType() == TokenType.LPARENT) {
            // Ident '(' [...] ')'情况, 表明调用了一个函数
            FunctionCall node = new FunctionCall();
            node.funcName = currentToken().getContent();
            consumeToken();  // ident
            consumeToken();  // (
            node.args = new ArrayList<>();
            if (currentToken().getType() != TokenType.RPARENT) {
                node.args.addAll(parseFuncRParams());
            }
            expect(TokenType.RPARENT, "j", true);
            exp = node;
        } else {
            // PrimaryExp -> '(' Exp ')' | LVal | Number
            // LVal -> Ident ['[' Exp ']']
            exp = parsePrimaryExp();
        }
        printComponent("UnaryExp");
        return exp;
    }

    // FuncRParams -> Exp { ',' Exp }
    private List<ExprNode> parseFuncRParams() {
        List<ExprNode> args = new ArrayList<>();
        args.add(parseExp());
        while (currentToken().getType() == TokenType.COMMA) {
            consumeToken();
            args.add(parseExp());
        }
        printComponent("FuncRParams");
        return args;
    }

    // PrimaryExp -> '(' Exp ')' | LVal | Number
    private ExprNode parsePrimaryExp() {
        ExprNode exp;
        if (currentToken().getType() == TokenType.LPARENT) {
            // ‘(’ Exp ‘)’ 情况
            consumeToken();
            ExprNode node = parseExp();
            expect(TokenType.RPARENT, "j", true);
            exp = node;
        } else if(currentToken().getType() == TokenType.INTCON) {
            // Number情况
            Number node = new Number();
            node.value = Integer.parseInt(currentToken().getContent());
            consumeToken();
            exp = node;
            printComponent("Number");
        } else {
            // LVal
            exp = parseLVal();
        }
        printComponent("PrimaryExp");
        return exp;
    }

    // LVal -> Ident ['[' Exp ']']
    private ExprNode parseLVal() {
        LVal node = new LVal();
        node.identName = currentToken().getContent();
        expect(TokenType.IDENFR, "ident_error", false);
        if (currentToken().getType() == TokenType.LBRACK) {
            consumeToken();
            node.arrayIndex = parseExp();
            expect(TokenType.RBRACK, "k", true);
        }
        printComponent("LVal");
        return node;
    }
}
