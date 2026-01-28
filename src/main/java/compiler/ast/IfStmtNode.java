// If statement
package compiler.ast;

public class IfStmtNode extends StmtNode {
    public final ExprNode condition;
    public final StmtNode thenBranch;
    public final StmtNode elseBranch;

    public IfStmtNode(ExprNode condition, StmtNode thenBranch, StmtNode elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
