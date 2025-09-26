import frontend.Lexer;
import frontend.Token;
import frontend.TokenType;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Compiler {

    private final String sourceCode;
    private Lexer lexer;
    private String lexerString = "";
    private String error = "";

    public Compiler(String sourceCode) {
        this.sourceCode = sourceCode;
        this.lexer = new Lexer(sourceCode);
    }

    public void compile() {
        Token token;
        // 词法分析
        do {
            token = lexer.getNextToken();
            //System.out.println(token);
            if (token.getType() == TokenType.EOFSY) {
                break;
            } else if (token.getType() == TokenType.ANNOSY) {
                continue;
            } else if (token.getType() == TokenType.ERRORSY) {
                error += token.toError();
                continue;
            }

            lexerString += token.toString();
        } while (true);
    }

    public static void main(String[] args) {
        String inputPath = "testfile.txt";
        String outputPath = "lexer.txt";
        String errorPath = "error.txt";
        try {
            String sourceCode = new String(Files.readAllBytes(Paths.get(inputPath)));
            //System.out.println(sourceCode);
            Compiler compiler = new Compiler(sourceCode);
            compiler.compile();

            if (!compiler.error.isEmpty()) {
                Files.write(Paths.get(errorPath), compiler.error.getBytes());
            } else {
                Files.write(Paths.get(outputPath), compiler.lexerString.getBytes());
            }
            //System.out.print(compiler.output);
        } catch (IOException e) {
            System.err.println("Error reading or writing file: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Compilation Error: " + e.getMessage());
        }

    }
}
