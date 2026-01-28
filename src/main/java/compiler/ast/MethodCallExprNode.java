// Method call
package compiler.ast;

import java.util.List;

public class MethodCallExprNode extends ExprNode {
    public final String name;
    public final List<ExprNode> arguments;

    public MethodCallExprNode(String name, List<ExprNode> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
