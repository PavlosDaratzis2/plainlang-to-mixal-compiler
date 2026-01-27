// ProgramNode
package compiler.ast;

import java.util.List;

public class ProgramNode extends AstNode {
    public final List<MethodNode> methods;

    public ProgramNode(List<MethodNode> methods) {
        this.methods = methods;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
