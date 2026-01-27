// BlockNode
package compiler.ast;

import java.util.List;

public class BlockNode extends StmtNode {
    public final List<StmtNode> statements;

    public BlockNode(List<StmtNode> statements) {
        this.statements = statements;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
