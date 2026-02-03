package compiler.codegen;

import compiler.ast.*;

import java.util.*;

public class CodeGenerator implements AstVisitor<Void> {
    private final StringBuilder out = new StringBuilder();
    private int labelCounter = 0;
    private int tmpCounter = 0;
    private final Stack<String> breakLabels = new Stack<>();
    private final Map<String, Integer> variableAddresses = new HashMap<>();
    private int nextVariableAddress = 2000; // Start variable storage at address 2000
    private String currentMethod = "";
    private final int MAX_LABEL_LENGTH = 9; // Περιορισμός μήκους ετικετών

    private String newLabel() {
        return "L" + (labelCounter++);
    }

    private String newTemp() {
        return "TMP" + (tmpCounter++);
    }

    // Μέθοδος για τον περιορισμό του μήκους των ετικετών
    private String limitLabelLength(String label) {
        if (label.length() > MAX_LABEL_LENGTH) {
            return label.substring(0, MAX_LABEL_LENGTH);
        }
        return label;
    }

    public String generate(ProgramNode program) {
        out.append("START   ORIG 1000\n");
        out.append("        JMP MAIN\n");

        // Generate code for each method
        program.accept(this);

        // Generate variable and temporary storage area
        for (Map.Entry<String, Integer> entry : variableAddresses.entrySet()) {
            // Αποφεύγουμε τη χρήση ετικετών μιας γραμμής
            if (entry.getKey().length() > 1) {
                out.append(entry.getKey()).append("   ORIG ").append(entry.getValue()).append("\n");
                out.append("        CON 0\n");
            } else {
                out.append("        ORIG ").append(entry.getValue()).append("\n");
                out.append("        CON 0\n");
            }
        }

        // Αφαίρεση της τελευταίας αλλαγής γραμμής, αν υπάρχει
        String result = out.toString();
        if (result.endsWith("\n")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    @Override
    public Void visit(ProgramNode node) {
        for (MethodNode method : node.methods) {
            method.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(MethodNode node) {
        // Χρησιμοποιούμε απευθείας το όνομα χωρίς κάτω παύλες
        currentMethod = node.name.toUpperCase();
        out.append(currentMethod).append("   NOP\n");

        // Reserve space for parameters and set up parameter addresses
        int paramAddress = nextVariableAddress;
        for (ParamNode param : node.params) {
            // Δημιουργούμε το όνομα της παραμέτρου χωρίς κάτω παύλες
            String paramName = currentMethod + param.name.toUpperCase();
            variableAddresses.put(paramName, paramAddress);
            paramAddress += 1;
        }
        nextVariableAddress = paramAddress + node.params.size();

        // Process method body
        node.body.accept(this);

        // Add return handling
        String retLabel = limitLabelLength("RET" + currentMethod);
        out.append("        JMP ").append(retLabel).append("\n");
        out.append(retLabel).append("   NOP\n");
        return null;
    }

    @Override
    public Void visit(BlockNode node) {
        for (StmtNode stmt : node.statements) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(DeclNode node) {
        // Δημιουργία της μεταβλητής στη μνήμη
        allocateVariable(node.name);

        // Αν υπάρχει αρχική τιμή, αρχικοποίηση της μεταβλητής
        if (node.initialValue != null) {
            node.initialValue.accept(this);
            out.append("        STA ").append(getVariableName(node.name)).append("\n");
        }

        return null;
    }

    @Override
    public Void visit(AssignStmtNode node) {
        // Generate code for expression
        node.expression.accept(this);

        // Store the result in the variable
        String varName = getVariableName(node.variable);
        out.append("        STA ").append(varName).append("\n");
        return null;
    }

    @Override
    public Void visit(ReturnStmtNode node) {
        // Evaluate the expression and leave result in register A
        node.expression.accept(this);
        String retLabel = limitLabelLength("RET" + currentMethod);
        out.append("        JMP ").append(retLabel).append("\n");
        return null;
    }

    @Override
    public Void visit(IfStmtNode node) {
        String elseLabel = newLabel();
        String endLabel = newLabel();

        // Generate code for the condition
        node.condition.accept(this);

        // If condition is false (A == 0), jump to else part
        // Χρησιμοποιούμε JE αντί για JZ
        out.append("        JE ").append(elseLabel).append("\n");

        // Generate code for the then branch
        node.thenBranch.accept(this);
        out.append("        JMP ").append(endLabel).append("\n");

        // Generate code for the else branch
        out.append(elseLabel).append("   NOP\n");
        node.elseBranch.accept(this);

        out.append(endLabel).append("   NOP\n");
        return null;
    }

    @Override
    public Void visit(WhileStmtNode node) {
        String startLabel = newLabel();
        String endLabel = newLabel();

        // Push the end label onto the break stack
        breakLabels.push(endLabel);

        // Loop start
        out.append(startLabel).append("   NOP\n");

        // Generate code for the condition
        node.condition.accept(this);

        // If condition is false (A == 0), exit the loop
        // Χρησιμοποιούμε JE αντί για JZ
        out.append("        JE ").append(endLabel).append("\n");

        // Generate code for the loop body
        node.body.accept(this);

        // Jump back to the loop start
        out.append("        JMP ").append(startLabel).append("\n");

        // Loop end
        out.append(endLabel).append("   NOP\n");

        // Pop the break label from the stack
        breakLabels.pop();
        return null;
    }

    @Override
    public Void visit(BreakStmtNode node) {
        if (breakLabels.isEmpty()) {
            throw new RuntimeException("Break statement outside of loop");
        }

        // Jump to the end of the current loop
        out.append("        JMP ").append(breakLabels.peek()).append("\n");
        return null;
    }

    @Override
    public Void visit(BinaryExprNode node) {
        String temp = newTemp();
        allocateVariable(temp);

        // Handle different types of binary operations
        if (node.op.equals("+") || node.op.equals("-") || node.op.equals("*") || node.op.equals("/")) {
            // Arithmetic operation

            // Evaluate left operand and store it
            node.left.accept(this);
            out.append("        STA ").append(temp).append("\n");

            // Evaluate right operand
            node.right.accept(this);

            // Perform the arithmetic operation
            switch (node.op) {
                case "+":
                    out.append("        LDA ").append(temp).append("\n");
                    out.append("        ADD 0,1\n"); // Add right operand from register A
                    break;
                case "-":
                    out.append("        STX 0,1\n"); // Store right operand
                    out.append("        LDA ").append(temp).append("\n");
                    out.append("        SUB 0,1\n"); // Subtract right operand
                    break;
                case "*":
                    out.append("        MUL ").append(temp).append("\n");
                    break;
                case "/":
                    out.append("        STX 0,1\n"); // Store right operand
                    out.append("        LDA ").append(temp).append("\n");
                    out.append("        DIV 0,1\n"); // Divide by right operand
                    break;
            }
        } else {
            // Comparison operation

            // Evaluate left operand and store it
            node.left.accept(this);
            out.append("        STA ").append(temp).append("\n");

            // Evaluate right operand
            node.right.accept(this);

            // Store right operand
            String temp2 = newTemp();
            allocateVariable(temp2);
            out.append("        STA ").append(temp2).append("\n");

            // Perform the comparison
            out.append("        LDA ").append(temp).append("\n");
            out.append("        CMPA ").append(temp2).append("\n");

            String trueLabel = newLabel();
            String endLabel = newLabel();

            switch (node.op) {
                case "==":
                    out.append("        JE ").append(trueLabel).append("\n");
                    break;
                case "!=":
                    out.append("        JNE ").append(trueLabel).append("\n");
                    break;
                case "<":
                    out.append("        JL ").append(trueLabel).append("\n");
                    break;
                case "<=":
                    out.append("        JLE ").append(trueLabel).append("\n");
                    break;
                case ">":
                    out.append("        JG ").append(trueLabel).append("\n");
                    break;
                case ">=":
                    out.append("        JGE ").append(trueLabel).append("\n");
                    break;
            }

            // Comparison is false
            out.append("        LDA 0\n");
            out.append("        JMP ").append(endLabel).append("\n");

            // Comparison is true
            out.append(trueLabel).append("   NOP\n");
            out.append("        LDA 1\n");

            out.append(endLabel).append("   NOP\n");
        }

        return null;
    }

    @Override
    public Void visit(UnaryExprNode node) {
        if (node.op.equals("-")) {
            // Υλοποίηση του μοναδιαίου αρνητικού προσήμου
            node.expr.accept(this);
            out.append("        STX 0,1\n"); // Αποθήκευση της τιμής της έκφρασης
            out.append("        LDA 0\n"); // Φόρτωση του 0
            out.append("        SUB 0,1\n"); // Αφαίρεση της τιμής της έκφρασης (0 - expr)
        } else {
            // Άλλοι μοναδιαίοι τελεστές
            node.expr.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(VarExprNode node) {
        String varName = getVariableName(node.name);
        out.append("        LDA ").append(varName).append("\n");
        return null;
    }

    @Override
    public Void visit(LiteralExprNode node) {
        // Χρησιμοποιούμε τις σταθερές χωρίς =
        out.append("        LDA ").append(node.value).append("\n");
        return null;
    }

    @Override
    public Void visit(MethodCallExprNode node) {
        // Save registers before call
        // Χρησιμοποιούμε SAVEDREG αντί για SAVED_J
        out.append("        STJ SAVEDREG\n");
        // Βεβαιωνόμαστε ότι το SAVEDREG είναι δηλωμένο
        allocateVariable("SAVEDREG");

        // Push arguments onto the stack
        for (int i = 0; i < node.arguments.size(); i++) {
            ExprNode arg = node.arguments.get(i);
            arg.accept(this);
            String paramVar = "PARAM" + i;
            allocateVariable(paramVar);
            out.append("        STA ").append(paramVar).append("\n");
        }

        // Call the method
        out.append("        JSJ ").append(node.name.toUpperCase()).append("\n");

        // Restore registers after return
        // Χρησιμοποιούμε LD6 αντί για LDJ
        out.append("        LD6 SAVEDREG\n");

        // Result is already in register A
        return null;
    }

    // Helper method to get variable name with proper method scope
    private String getVariableName(String varName) {
        // Δημιουργούμε το όνομα της μεταβλητής χωρίς κάτω παύλες
        String scopedName = currentMethod + varName.toUpperCase();

        // Allocate variable if it hasn't been allocated yet
        if (!variableAddresses.containsKey(scopedName)) {
            allocateVariable(scopedName);
        }

        return scopedName;
    }

    // Helper method to allocate variable storage
    private void allocateVariable(String varName) {
        if (!variableAddresses.containsKey(varName)) {
            variableAddresses.put(varName, nextVariableAddress);
            nextVariableAddress += 1;
        }
    }
}