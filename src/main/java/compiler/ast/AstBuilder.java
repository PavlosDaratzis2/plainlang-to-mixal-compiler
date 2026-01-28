package compiler.ast;

import compiler.parser.PlainLangBaseVisitor;
import compiler.parser.PlainLangParser;

import java.util.ArrayList;
import java.util.List;

public class AstBuilder extends PlainLangBaseVisitor<AstNode> {

    @Override
    public AstNode visitProgram(PlainLangParser.ProgramContext ctx) {
        List<MethodNode> methods = new ArrayList<>();
        if (ctx.methodList() != null) {
            for (PlainLangParser.MethodContext mctx : ctx.methodList().method()) {
                methods.add((MethodNode) visit(mctx));
            }
        }
        return new ProgramNode(methods);
    }

    @Override
    public AstNode visitMethod(PlainLangParser.MethodContext ctx) {
        String returnType = ctx.type().getText();
        String name = ctx.ID().getText();
        List<ParamNode> params = new ArrayList<>();

        if (ctx.params() != null) {
            for (PlainLangParser.ParamContext pctx : ctx.params().param()) {
                String type = pctx.type().getText();
                String id = pctx.ID().getText();
                params.add(new ParamNode(type, id));
            }
        }

        BlockNode body = (BlockNode) visit(ctx.body());
        return new MethodNode(returnType, name, params, body);
    }

    @Override
    public AstNode visitBody(PlainLangParser.BodyContext ctx) {
        List<StmtNode> stmts = new ArrayList<>();

        // Επεξεργασία δηλώσεων μεταβλητών
        if (ctx.decls() != null) {
            stmts.addAll(processDecls(ctx.decls()));
        }

        // Επεξεργασία εντολών
        if (ctx.stmts() != null) {
            for (PlainLangParser.StmtContext stmt : ctx.stmts().stmt()) {
                StmtNode stmtNode = (StmtNode) visit(stmt);
                if (stmtNode != null) {
                    stmts.add(stmtNode);
                }
            }
        }

        return new BlockNode(stmts);
    }

    private List<StmtNode> processDecls(PlainLangParser.DeclsContext ctx) {
        List<StmtNode> declNodes = new ArrayList<>();

        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof PlainLangParser.DeclContext) {
                PlainLangParser.DeclContext declCtx = (PlainLangParser.DeclContext) ctx.getChild(i);
                declNodes.add((StmtNode) visit(declCtx));
            }
        }

        return declNodes;
    }

    @Override
    public AstNode visitDecl(PlainLangParser.DeclContext ctx) {
        String type = ctx.type().getText();
        String id = ctx.ID().getText();

        ExprNode initExpr = null;
        if (ctx.expr() != null) {
            initExpr = (ExprNode) visit(ctx.expr());
        }

        DeclNode mainDecl = new DeclNode(type, id, initExpr);

        // Επεξεργασία πρόσθετων μεταβλητών στην ίδια δήλωση
        if (ctx.vars() != null && ctx.vars().ID() != null && !ctx.vars().ID().isEmpty()) {
            // Εδώ θα επεξεργαστούμε τις πρόσθετες μεταβλητές, αλλά για απλότητα
            // επιστρέφουμε μόνο την κύρια δήλωση
        }

        return mainDecl;
    }

    @Override
    public AstNode visitAssignStmt(PlainLangParser.AssignStmtContext ctx) {
        String varName = ctx.location().getText();
        ExprNode expr = (ExprNode) visit(ctx.expr());
        return new AssignStmtNode(varName, expr);
    }

    @Override
    public AstNode visitStmt(PlainLangParser.StmtContext ctx) {
        if (ctx.assignStmt() != null) {
            return visit(ctx.assignStmt());
        } else if (ctx.getChild(0).getText().equals("return")) {
            return new ReturnStmtNode((ExprNode) visit(ctx.expr()));
        } else if (ctx.getChild(0).getText().equals("if")) {
            ExprNode condition = (ExprNode) visit(ctx.expr());
            StmtNode thenStmt = (StmtNode) visit(ctx.stmt(0));
            StmtNode elseStmt = (StmtNode) visit(ctx.stmt(1));
            return new IfStmtNode(condition, thenStmt, elseStmt);
        } else if (ctx.getChild(0).getText().equals("while")) {
            ExprNode condition = (ExprNode) visit(ctx.expr());
            StmtNode body = (StmtNode) visit(ctx.stmt(0));
            return new WhileStmtNode(condition, body);
        } else if (ctx.getChild(0).getText().equals("break")) {
            return new BreakStmtNode();
        } else if (ctx.block() != null) {
            return visit(ctx.block());
        }
        return null; // empty ;
    }

    @Override
    public AstNode visitExpr(PlainLangParser.ExprContext ctx) {
        return visit(ctx.cmpExpr());
    }

    @Override
    public AstNode visitCmpExpr(PlainLangParser.CmpExprContext ctx) {
        ExprNode left = (ExprNode) visit(ctx.addExpr(0));

        if (ctx.relop() != null) {
            String op = ctx.relop().getText();
            ExprNode right = (ExprNode) visit(ctx.addExpr(1));
            return new BinaryExprNode(left, op, right);
        }

        return left;
    }

    @Override
    public AstNode visitAddExpr(PlainLangParser.AddExprContext ctx) {
        ExprNode result = (ExprNode) visit(ctx.multExpr(0));

        for (int i = 0; i < ctx.addop().size(); i++) {
            String op = ctx.addop(i).getText();
            ExprNode right = (ExprNode) visit(ctx.multExpr(i+1));
            result = new BinaryExprNode(result, op, right);
        }

        return result;
    }

    @Override
    public AstNode visitMultExpr(PlainLangParser.MultExprContext ctx) {
        ExprNode result = (ExprNode) visit(ctx.unaryExpr(0));

        for (int i = 0; i < ctx.mulop().size(); i++) {
            String op = ctx.mulop(i).getText();
            ExprNode right = (ExprNode) visit(ctx.unaryExpr(i+1));
            result = new BinaryExprNode(result, op, right);
        }

        return result;
    }

    @Override
    public AstNode visitUnaryExpr(PlainLangParser.UnaryExprContext ctx) {
        if (ctx.getChildCount() == 2) {
            // Περίπτωση μοναδιαίου τελεστή -
            String op = ctx.getChild(0).getText();
            ExprNode expr = (ExprNode) visit(ctx.unaryExpr());
            return new UnaryExprNode(op, expr);
        } else {
            // Περίπτωση primaryExpr
            return visit(ctx.primaryExpr());
        }
    }

    @Override
    public AstNode visitPrimaryExpr(PlainLangParser.PrimaryExprContext ctx) {
        if (ctx.expr() != null) {
            return visit(ctx.expr());
        } else if (ctx.methodCall() != null) {
            return visit(ctx.methodCall());
        } else if (ctx.location() != null) {
            return new VarExprNode(ctx.location().ID().getText());
        } else if (ctx.INTEGER() != null) {
            return new LiteralExprNode(ctx.INTEGER().getText());
        } else if (ctx.getText().equals("true")) {
            return new LiteralExprNode("1");
        } else if (ctx.getText().equals("false")) {
            return new LiteralExprNode("0");
        }

        return null;
    }

    @Override
    public AstNode visitMethodCall(PlainLangParser.MethodCallContext ctx) {
        String methodName = ctx.ID().getText();

        List<ExprNode> args = new ArrayList<>();
        if (ctx.actuals() != null) {
            for (PlainLangParser.ExprContext expr : ctx.actuals().expr()) {
                args.add((ExprNode) visit(expr));
            }
        }

        return new MethodCallExprNode(methodName, args);
    }

    @Override
    public AstNode visitBlock(PlainLangParser.BlockContext ctx) {
        List<StmtNode> stmts = new ArrayList<>();
        if (ctx.stmts() != null) {
            for (PlainLangParser.StmtContext stmt : ctx.stmts().stmt()) {
                StmtNode stmtNode = (StmtNode) visit(stmt);
                if (stmtNode != null) {
                    stmts.add(stmtNode);
                }
            }
        }
        return new BlockNode(stmts);
    }
}