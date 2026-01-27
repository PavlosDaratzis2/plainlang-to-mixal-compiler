package compiler.ast;

public interface AstVisitor<T> {
    T visit(ProgramNode node);
    T visit(MethodNode node);
    T visit(BlockNode node);
    T visit(AssignStmtNode node);
    T visit(ReturnStmtNode node);
    T visit(IfStmtNode node);
    T visit(WhileStmtNode node);
    T visit(BreakStmtNode node);
    T visit(BinaryExprNode node);
    T visit(VarExprNode node);
    T visit(LiteralExprNode node);
    T visit(MethodCallExprNode node);
    T visit(DeclNode node);
    T visit(UnaryExprNode node);
}