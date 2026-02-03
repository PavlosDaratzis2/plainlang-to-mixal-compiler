package compiler;

import compiler.ast.AstBuilder;
import compiler.ast.ProgramNode;
import compiler.codegen.CodeGenerator;
import compiler.parser.PlainLangLexer;
import compiler.parser.PlainLangParser;
import compiler.semantics.SemanticAnalyzer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.BitSet;

public class Main {
    public static void main(String[] args) {
        String[] filesToCompile = {
                "program1.plain",
                "program2.plain",
                "program3.plain",
                "factorial.plain",
                "error1.plain",
                "error2.plain"
        };

        // Αν δοθούν ορίσματα γραμμής εντολών, χρησιμοποίησέ τα
        if (args.length > 0) {
            filesToCompile = new String[] { args[0] };
        }

        for (String inputFile : filesToCompile) {
            try {
                System.out.println("\n=== Compiling " + inputFile + " ===");

                String outputFile = inputFile.replace(".plain", ".mix");

                // Διάβασμα του αρχείου εισόδου
                String input;
                try {
                    input = new String(Files.readAllBytes(Paths.get(inputFile)), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    System.err.println("Error reading file " + inputFile + ": " + e.getMessage());
                    continue;
                }

                // Δημιουργία του error listener
                ErrorListener errorListener = new ErrorListener();

                // Ρύθμιση του lexer και parser με το error listener
                PlainLangLexer lexer = new PlainLangLexer(CharStreams.fromString(input));
                lexer.removeErrorListeners();
                lexer.addErrorListener(errorListener);

                CommonTokenStream tokens = new CommonTokenStream(lexer);

                PlainLangParser parser = new PlainLangParser(tokens);
                parser.removeErrorListeners();
                parser.addErrorListener(errorListener);

                // Ανάλυση του προγράμματος
                PlainLangParser.ProgramContext programContext = parser.program();

                if (errorListener.hasErrors()) {
                    System.err.println("Compilation failed due to syntax errors.");
                    continue;
                }

                // Δημιουργία του AST
                System.out.println("Building Abstract Syntax Tree...");
                AstBuilder builder = new AstBuilder();
                ProgramNode ast;
                try {
                    ast = (ProgramNode) builder.visit(programContext);
                } catch (Exception e) {
                    System.err.println("Error building AST: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }

                // Σημασιολογική ανάλυση
                System.out.println("Performing semantic analysis...");
                SemanticAnalyzer analyzer = new SemanticAnalyzer();
                try {
                    analyzer.analyze(ast);
                } catch (RuntimeException e) {
                    System.err.println("Semantic error: " + e.getMessage());
                    continue;
                }

                // Παραγωγή κώδικα MIXAL
                System.out.println("Generating MIXAL code...");
                CodeGenerator generator = new CodeGenerator();
                String mixalCode;
                try {
                    mixalCode = generator.generate(ast);
                } catch (Exception e) {
                    System.err.println("Error generating MIXAL code: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }

                // Αποθήκευση του παραγόμενου κώδικα
                try {
                    Files.write(Paths.get(outputFile), mixalCode.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    System.err.println("Error writing output file " + outputFile + ": " + e.getMessage());
                    continue;
                }

                System.out.println("Compilation successful. MIXAL code written to " + outputFile);

                // Εμφάνιση του παραγόμενου κώδικα
                System.out.println("\nGenerated MIXAL code:");
                System.out.println("------------------------");
                System.out.println(mixalCode);
                System.out.println("------------------------");

            } catch (Exception e) {
                System.err.println("Error processing " + inputFile + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Custom error listener
    private static class ErrorListener implements ANTLRErrorListener {
        private boolean hasErrors = false;

        public boolean hasErrors() {
            return hasErrors;
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg, RecognitionException e) {
            hasErrors = true;
            System.err.println("Line " + line + ":" + charPositionInLine + " - " + msg);
        }

        @Override
        public void reportAmbiguity(Parser parser, DFA dfa, int startIndex,
                                    int stopIndex, boolean exact, BitSet ambigAlts,
                                    ATNConfigSet configs) {
            // Not reporting ambiguities
        }

        @Override
        public void reportAttemptingFullContext(Parser parser, DFA dfa,
                                                int startIndex, int stopIndex,
                                                BitSet conflictingAlts, ATNConfigSet configs) {
            // Not reporting full context attempts
        }

        @Override
        public void reportContextSensitivity(Parser parser, DFA dfa,
                                             int startIndex, int stopIndex,
                                             int prediction, ATNConfigSet configs) {
            // Not reporting context sensitivity
        }
    }
}