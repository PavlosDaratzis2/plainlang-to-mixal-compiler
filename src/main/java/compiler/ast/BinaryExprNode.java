// Binary expression
package compiler.ast;

public class BinaryExprNode extends ExprNode {
    public final String op;
    public final ExprNode left;
    public final ExprNode right;

    public BinaryExprNode(ExprNode left, String op, ExprNode right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
