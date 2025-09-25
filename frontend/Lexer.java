package frontend;

enum TokenType {
    BEGINSY, ENDSY, FORSY, DOSY, IFSY, THENSY, ELSESY,
    IDSY, // 内部字符串(标识符
    INTSY, // 整数
    COLONSY, // :
    PLUSSY, // +
    STARSY, // *
    COMSY, //,
    LPARSY, // (
    RPARSY, // )
    ASSIGNSY, // :=
    EOFSY,
    ERRORSY
}

class Token {
    TokenType type;
    String value;
    int num;
}

public class Lexer {
    String sourceCode;
    int currentPos;
    public Lexer(String code) {
        this.sourceCode = code;
        this.currentPos = 0;
    }

    public boolean hasNext() {
        return currentPos < sourceCode.length();
    }

    public Token getNextToken() {
        Token token = new Token();

        if (!hasNext()) {
            token.type = TokenType.EOFSY;
            token.value = "EOF";
            return token;
        }
        // 跳过空白
        while(sourceCode.charAt(this.currentPos) == ' ') {
            currentPos++;
        }
        if (Character.isLetter(sourceCode.charAt(currentPos))) {
            StringBuilder sb = new StringBuilder();
            while ((Character.isDigit(sourceCode.charAt(currentPos)) || Character.isLetter(sourceCode.charAt(currentPos)) && hasNext())) {
                sb.append(sourceCode.charAt(currentPos));
                currentPos++;
            }
            //currentPos--;
            token.value = sb.toString();
            if (token.value.equals("BEGIN")) {
                token.type = TokenType.BEGINSY;
            } else if (token.value.equals("END")) {
                token.type = TokenType.ENDSY;
            } else if (token.value.equals("FOR")) {
                token.type = TokenType.FORSY;
            } else if (token.value.equals("DO")) {
                token.type = TokenType.DOSY;
            } else if (token.value.equals("IF")) {
                token.type = TokenType.IFSY;
            } else if (token.value.equals("THEN")) {
                token.type = TokenType.THENSY;
            } else if (token.value.equals("ELSE")) {
                token.type = TokenType.ELSESY;
            } else {
                token.type = TokenType.IDSY;
            }

        } else if (sourceCode.charAt(currentPos) >= '0' && sourceCode.charAt(currentPos) <= '9' && hasNext()) {
            StringBuilder sb = new StringBuilder();
            while (Character.isDigit(sourceCode.charAt(currentPos))) {
                sb.append(sourceCode.charAt(currentPos));
                currentPos++;
            }
            //currentPos--;
            token.type = TokenType.INTSY;
            token.value = sb.toString();
            token.num = Integer.parseInt(sb.toString());

        } else if (sourceCode.charAt(currentPos) == '+') {
            token.type = TokenType.PLUSSY;
            token.value = String.valueOf(sourceCode.charAt(currentPos));
            currentPos++;

        } else if (sourceCode.charAt(currentPos) == '*') {
            token.type = TokenType.STARSY;
            token.value = String.valueOf(sourceCode.charAt(currentPos));
            currentPos++;

        } else if (sourceCode.charAt(currentPos) == ',') {
            token.type = TokenType.COMSY;
            token.value = String.valueOf(sourceCode.charAt(currentPos));
            currentPos++;

        } else if (sourceCode.charAt(currentPos) == '(') {
            token.type = TokenType.LPARSY;
            token.value = String.valueOf(sourceCode.charAt(currentPos));
            currentPos++;

        } else if (sourceCode.charAt(currentPos) == ')') {
            token.type = TokenType.RPARSY;
            token.value = String.valueOf(sourceCode.charAt(currentPos));
            currentPos++;

        } else if (sourceCode.charAt(currentPos) == ':') {
            if (sourceCode.charAt(currentPos + 1) == '=') {
                token.type = TokenType.ASSIGNSY;
                token.value = sourceCode.substring(currentPos, currentPos + 2);
                currentPos+=2;
            } else {
                token.type = TokenType.COLONSY;
                token.value = String.valueOf(sourceCode.charAt(currentPos));
                currentPos++;
            }

        } else {
            token.type = TokenType.ERRORSY;
            token.value = String.valueOf(sourceCode.charAt(currentPos));
            currentPos++;
        }

        //System.out.println(currentPos);ni
        return token;
    }

    public static void main(String[] args) {
        String s = "FOR i := 10 DO BEGIN count (:= count * 1) END;";
        Lexer lexer = new Lexer(s);
        System.out.printf("Source Code: %s\n\n", s);
        System.out.printf("%-10s | %-10s | %s\n", "Type", "Value", "Integer Value");
        System.out.printf("-----------------------------------------\n");

        Token token = lexer.getNextToken();
        while (token.type != TokenType.EOFSY) {
            if (token.type == TokenType.INTSY) {
                System.out.printf("%-10s | %-10s | %d\n", token.type, token.value, token.num);
            } else {
                System.out.printf("%-10s | %-10s |\n", token.type, token.value);
            }
            token = lexer.getNextToken();
        }
    }
}


