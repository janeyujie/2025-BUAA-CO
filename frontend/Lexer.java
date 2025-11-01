package frontend;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String sourceCode;
    private String outputString = "";
    private final List<SyntaxError> errors = new ArrayList<>();
    private final List<Token> tokens = new ArrayList<>();

    private int currentPos = 0;
    private int line = 1;
    public Lexer(String code) {
        this.sourceCode = code;
    }

    public List<Token> getTokens() {
        Token token;
        do {
            token = getNextToken();
            if (token.getType() == TokenType.EOFSY) {
                tokens.add(token);
                outputString += token.toString();
                break;
            } else if (token.getType() == TokenType.ANNOSY) {
                continue;
            } else if (token.getType() == TokenType.ERRORSY) {
                //lexicalError += token.toError();
                errors.add(new SyntaxError(token.getLineno(), token.getContent()));
                continue;
            }
            tokens.add(token);
            outputString += token.toString();
        } while (true);
        return tokens;
    }

    public String getOutputString() {
        return outputString;
    }

    public List<SyntaxError> getErrors() {
        return errors;
    }

    public boolean hasNext() {
        return currentPos < sourceCode.length();
    }

    public Token getNextToken() {
        // 跳过空白
        skipWhitespace();
        // 读到文件最后输出EOF
        //System.out.println(currentPos);
        if (!hasNext()) {
            return new Token(TokenType.EOFSY, TokenType.EOFSY.toString(), line);
        }
        char currentChar = sourceCode.charAt(currentPos);
        //System.out.println(currentChar);
        if (Character.isLetter(currentChar) || currentChar == '_') {
            int start = currentPos;
            while (hasNext() && (Character.isDigit(sourceCode.charAt(currentPos)) || Character.isLetter(sourceCode.charAt(currentPos)) || sourceCode.charAt(currentPos) == '_')) {
                currentPos++;
            }
            String lexeme = sourceCode.substring(start, currentPos);

            if (lexeme.equals(TokenType.CONSTTK.toString())) {
                return new Token(TokenType.CONSTTK, lexeme, line);
            } else if (lexeme.equals(TokenType.INTTK.toString())) {
                return new Token(TokenType.INTTK, lexeme, line);
            } else if (lexeme.equals(TokenType.STATICTK.toString())) {
                return new Token(TokenType.STATICTK, lexeme, line);
            } else if (lexeme.equals(TokenType.BREAKTK.toString())) {
                return new Token(TokenType.BREAKTK, lexeme, line);
            } else if (lexeme.equals(TokenType.CONTINUETK.toString())) {
                return new Token(TokenType.CONTINUETK, lexeme, line);
            } else if (lexeme.equals(TokenType.IFTK.toString())) {
                return new Token(TokenType.IFTK, lexeme, line);
            } else if (lexeme.equals(TokenType.MAINTK.toString())) {
                return new Token(TokenType.MAINTK, lexeme, line);
            } else if (lexeme.equals(TokenType.ELSETK.toString())) {
                return new Token(TokenType.ELSETK, lexeme, line);
            } else if (lexeme.equals(TokenType.FORTK.toString())) {
                return new Token(TokenType.FORTK, lexeme, line);
            } else if (lexeme.equals(TokenType.RETURNTK.toString())) {
                return new Token(TokenType.RETURNTK, lexeme, line);
            } else if (lexeme.equals(TokenType.VOIDTK.toString())) {
                return new Token(TokenType.VOIDTK, lexeme, line);
            } else if (lexeme.equals(TokenType.PRINTFTK.toString())){
                return new Token(TokenType.PRINTFTK, lexeme, line);
            } else {
                return new Token(TokenType.IDENFR, lexeme, line);
                //token.setType(TokenType.IDENT);
            }

        } else if (Character.isDigit(currentChar)) {
            int start = currentPos;
            while (hasNext() && Character.isDigit(sourceCode.charAt(currentPos))) {
                currentPos++;
            }
            //currentPos--;
            String lexeme = sourceCode.substring(start, currentPos);
            return new Token(TokenType.INTCON, lexeme, line);

        } else if (currentChar == '+') {
            currentPos++;
            return new Token(TokenType.PLUS, String.valueOf(currentChar), line);

        } else if (currentChar == '-') {
            currentPos++;
            return new Token(TokenType.MINU, String.valueOf(currentChar), line);
        } else if (currentChar == '*') {
            currentPos++;
            return new Token(TokenType.MULT, String.valueOf(currentChar), line);
        } else if (currentChar == '%') {
            currentPos++;
            return new Token(TokenType.MOD, String.valueOf(currentChar), line);
        } else if (currentChar == ';') {
            currentPos++;
            return new Token(TokenType.SEMICN, String.valueOf(currentChar), line);
        } else if (currentChar == ',') {
            currentPos++;
            return new Token(TokenType.COMMA, String.valueOf(currentChar), line);
        } else if (currentChar == '(') {
            currentPos++;
            return new Token(TokenType.LPARENT, String.valueOf(currentChar), line);
        } else if (currentChar == ')') {
            currentPos++;
            return new Token(TokenType.RPARENT, String.valueOf(currentChar), line);
        } else if (currentChar == '{') {
            currentPos++;
            return new Token(TokenType.LBRACE, String.valueOf(currentChar), line);
        } else if (currentChar == '}') {
            currentPos++;
            return new Token(TokenType.RBRACE, String.valueOf(currentChar), line);
        } else if (currentChar == '[') {
            currentPos++;
            return new Token(TokenType.LBRACK, String.valueOf(currentChar), line);
        } else if (currentChar == ']') {
            currentPos++;
            return new Token(TokenType.RBRACK, String.valueOf(currentChar), line);
        } else if (currentChar == '!') {
            currentPos++;
            if (sourceCode.charAt(currentPos) == '=') {
                currentPos++;
                return new Token(TokenType.NEQ, sourceCode.substring(currentPos-2, currentPos), line);
            } else {
                return new Token(TokenType.NOT, String.valueOf(currentChar), line);
            }
        } else if (currentChar == '=') {
            currentPos++;
            if (sourceCode.charAt(currentPos) == '=') {
                currentPos++;
                return new Token(TokenType.EQL, sourceCode.substring(currentPos-2, currentPos), line);
            } else {
                return new Token(TokenType.ASSIGN, String.valueOf(currentChar), line);
            }
        } else if (currentChar == '<') {
            currentPos++;
            if (sourceCode.charAt(currentPos) == '=') {
                currentPos++;
                return new Token(TokenType.LEQ, sourceCode.substring(currentPos-2, currentPos), line);
            } else {
                return new Token(TokenType.LSS, String.valueOf(currentChar), line);
            }
        } else if (currentChar == '>') {
            currentPos++;
            if (sourceCode.charAt(currentPos) == '=') {
                currentPos++;
                return new Token(TokenType.GEQ, sourceCode.substring(currentPos-2, currentPos), line);
            } else {
                return new Token(TokenType.GRE, String.valueOf(currentChar), line);
            }
        } else if (currentChar == '&') {
            currentPos++;
            if (sourceCode.charAt(currentPos) == '&') {
                currentPos++;
                return new Token(TokenType.AND, sourceCode.substring(currentPos-2, currentPos), line);
            } else {
                // TODO: 错误处理
                errors.add(new SyntaxError(line, "a"));
                return new Token(TokenType.AND, "&", line);
            }
        } else if (currentChar == '|') {
            currentPos++;
            if (sourceCode.charAt(currentPos) == '|') {
                currentPos++;
                return new Token(TokenType.OR, sourceCode.substring(currentPos-2, currentPos), line);
            } else {
                // TODO: 错误处理
                errors.add(new SyntaxError(line, "a"));
                return new Token(TokenType.OR, "|", line);
            }
        } else if (currentChar == '/') {
            // 三种情况，DIV，行级注释和块级注释
            currentPos++;
            if (sourceCode.charAt(currentPos) == '/') {
                while (hasNext() && sourceCode.charAt(currentPos) != '\n') {
                    currentPos++;
                    //System.out.println(currentPos);
                }
                return new Token(TokenType.ANNOSY, "", line);
            } else if (sourceCode.charAt(currentPos) == '*') {
                while (hasNext()) {
                    currentPos++;
                    if (sourceCode.charAt(currentPos) == '*' && hasNext() && sourceCode.charAt(currentPos+1) == '/') {
                        currentPos+=2;
                        break;
                    }
                }
                return new Token(TokenType.ANNOSY, "", line);
            } else {
                return new Token(TokenType.DIV, String.valueOf(currentChar), line);
            }
        } else if (currentChar == '"') {
            // 只有可能是字符串常量
            int start = currentPos;
            currentPos++;
            while (hasNext() && sourceCode.charAt(currentPos) != '"') {
                //System.out.println(sourceCode.charAt(currentPos));
                currentPos++;
            }
            currentPos++;
            // 字符串常量中也包含引号
            String lexeme = sourceCode.substring(start, currentPos);
            return new Token(TokenType.STRCON, lexeme, line);
        }
        return null;
    }

    // TODO: 一个跳过空白的函数（换行需要单独识别，用来增加行号
    private void skipWhitespace() {
        while (currentPos < sourceCode.length()) {
            char ch = sourceCode.charAt(currentPos);
            if (ch == ' ' || ch == '\t' || ch == '\r') {
                currentPos++;
            } else if (ch == '\n') {
                currentPos++;
                line++;
            } else {
                break;
            }
        }
    }
}


