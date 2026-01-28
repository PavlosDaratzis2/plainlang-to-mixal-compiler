package compiler.ast;

public class UnaryExprNode extends ExprNode {
    public final String op;
    public final ExprNode expr;

    public UnaryExprNode(String op, ExprNode expr) {
        this.op = op;
        this.expr = expr;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}