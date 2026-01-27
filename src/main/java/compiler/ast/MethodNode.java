// MethodNode
package compiler.ast;

import java.util.List;

public class MethodNode extends AstNode {
    public final String returnType;
    public final String name;
    public final List<ParamNode> params;
    public final BlockNode body;

    public MethodNode(String returnType, String name, List<ParamNode> params, BlockNode body) {
        this.returnType = returnType;
        this.name = name;
        this.params = params;
        this.body = body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
