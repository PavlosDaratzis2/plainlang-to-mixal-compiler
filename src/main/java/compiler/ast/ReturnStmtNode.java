// Return statement
package compiler.ast;

public class ReturnStmtNode extends StmtNode {
    public final ExprNode expression;

    public ReturnStmtNode(ExprNode expression) {
        this.expression = expression;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
