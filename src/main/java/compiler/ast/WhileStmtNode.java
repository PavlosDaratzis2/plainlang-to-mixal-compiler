// While statement
package compiler.ast;

public class WhileStmtNode extends StmtNode {
    public final ExprNode condition;
    public final StmtNode body;

    public WhileStmtNode(ExprNode condition, StmtNode body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
