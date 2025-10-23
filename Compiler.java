import ast.Node;
import frontend.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Compiler {

    private final String sourceCode;
    List<SyntaxError> errors = new ArrayList<>();

    public Compiler(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public void compile() throws IOException {
        // 词法分析
        Lexer lexer = new Lexer(sourceCode);
        List<Token> tokens = lexer.getTokens();
        errors.addAll(lexer.getErrors());
        System.out.println(lexer.getOutputString());

        // 语法分析
        Parser parser = new Parser(tokens);
        Node tree = parser.parse(); // 启动语法分析，得到一个AST
        errors.addAll(parser.getErrors());

        // 输出
        if (!errors.isEmpty()) {
            writeErrors(errors);
        } else {
            String parserOutput = parser.getOutput();
            Files.write(Paths.get("parser.txt"), parserOutput.getBytes());
        }
    }

    private void writeErrors(List<SyntaxError> errors) throws IOException {
        Collections.sort(errors); //按行号排序
        StringBuilder errorOutput = new StringBuilder();
        for (SyntaxError error : errors) {
            errorOutput.append(error.toString()).append("\n");
        }
        Files.write(Paths.get("error.txt"), errorOutput.toString().getBytes());
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

            /*if (!compiler.lexicalError.isEmpty()) {
                Files.write(Paths.get(errorPath), compiler.lexicalError.getBytes());
            } else {
                Files.write(Paths.get(outputPath), compiler.lexerString.getBytes());
            }*/
            //System.out.print(compiler.output);
        } catch (IOException e) {
            System.err.println("Error reading or writing file: " + e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            //System.err.println("Compilation Error: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
