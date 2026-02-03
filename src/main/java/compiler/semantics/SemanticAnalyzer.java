package compiler.semantics;

import compiler.ast.*;
import compiler.symbols.*;

public class SemanticAnalyzer implements AstVisitor<Void> {
    private final MethodTable methodTable = new MethodTable();
    private SymbolTable currentScope;
    private int loopDepth = 0;

    public void analyze(ProgramNode program) {
        program.accept(this);
        if (!methodTable.hasMain()) {
            throw new RuntimeException("Missing main method.");
        }
    }

    @Override
    public Void visit(ProgramNode node) {
        // Πρώτο πέρασμα: καταχώρηση όλων των μεθόδων
        for (MethodNode method : node.methods) {
            if (!methodTable.declare(method.name, new MethodSignature(method.returnType, method.params.size()))) {
                throw new RuntimeException("Duplicate method: " + method.name);
            }
        }

        // Δεύτερο πέρασμα: ανάλυση σώματος κάθε μεθόδου
        for (MethodNode method : node.methods) {
            method.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(MethodNode node) {
        // Δημιουργία νέου πίνακα συμβόλων για τη μέθοδο
        currentScope = new SymbolTable();

        // Καταχώρηση παραμέτρων
        for (ParamNode param : node.params) {
            if (!currentScope.declare(param.name, param.type)) {
                throw new RuntimeException("Duplicate parameter: " + param.name);
            }
        }

        // Ανάλυση σώματος μεθόδου
        node.body.accept(this);
        return null;
    }

    @Override
    public Void visit(BlockNode node) {
        // Διασχίζουμε τις εντολές του block
        for (StmtNode stmt : node.statements) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(DeclNode node) {
        // Δήλωση της μεταβλητής στον πίνακα συμβόλων
        if (!currentScope.declare(node.name, node.type)) {
            throw new RuntimeException("Duplicate variable declaration: " + node.name);
        }

        // Αν υπάρχει αρχική τιμή, έλεγχος της έκφρασης
        if (node.initialValue != null) {
            node.initialValue.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(AssignStmtNode node) {
        // Έλεγχος αν η μεταβλητή έχει δηλωθεί
        if (currentScope.lookup(node.variable) == null) {
            throw new RuntimeException("Undeclared variable: " + node.variable);
        }

        // Ανάλυση της έκφρασης ανάθεσης
        node.expression.accept(this);
        return null;
    }

    @Override
    public Void visit(VarExprNode node) {
        // Έλεγχος αν η μεταβλητή έχει δηλωθεί
        if (currentScope.lookup(node.name) == null) {
            throw new RuntimeException("Undeclared variable: " + node.name);
        }
        return null;
    }

    @Override
    public Void visit(LiteralExprNode node) {
        // Τίποτα να ελέγξουμε για σταθερές
        return null;
    }

    @Override
    public Void visit(BinaryExprNode node) {
        // Ανάλυση αριστερής και δεξιάς πλευράς της έκφρασης
        node.left.accept(this);
        node.right.accept(this);
        return null;
    }

    @Override
    public Void visit(UnaryExprNode node) {
        // Ανάλυση της έκφρασης
        node.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(MethodCallExprNode node) {
        // Έλεγχος αν η μέθοδος είναι δηλωμένη
        MethodSignature sig = methodTable.lookup(node.name);
        if (sig == null) {
            throw new RuntimeException("Undefined method: " + node.name);
        }

        // Έλεγχος για σωστό αριθμό παραμέτρων
        if (sig.paramCount != node.arguments.size()) {
            throw new RuntimeException("Argument count mismatch in call to " + node.name +
                    ": expected " + sig.paramCount + ", got " + node.arguments.size());
        }

        // Ανάλυση των ορισμάτων
        for (ExprNode arg : node.arguments) {
            arg.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ReturnStmtNode node) {
        // Ανάλυση της τιμής επιστροφής
        node.expression.accept(this);
        return null;
    }

    @Override
    public Void visit(IfStmtNode node) {
        // Ανάλυση της συνθήκης
        node.condition.accept(this);

        // Ανάλυση του σώματος της if και else
        node.thenBranch.accept(this);
        node.elseBranch.accept(this);
        return null;
    }

    @Override
    public Void visit(WhileStmtNode node) {
        // Αύξηση του βάθους βρόχων (για τον έλεγχο του break)
        loopDepth++;

        // Ανάλυση της συνθήκης και του σώματος του βρόχου
        node.condition.accept(this);
        node.body.accept(this);

        // Μείωση του βάθους βρόχων
        loopDepth--;
        return null;
    }

    @Override
    public Void visit(BreakStmtNode node) {
        // Έλεγχος αν το break βρίσκεται μέσα σε βρόχο
        if (loopDepth == 0) {
            throw new RuntimeException("Break statement outside of loop");
        }
        return null;
    }
}