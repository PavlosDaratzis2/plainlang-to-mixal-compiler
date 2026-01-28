// Assign statement
package compiler.ast;

public class AssignStmtNode extends StmtNode {
    public final String variable;
    public final ExprNode expression;

    public AssignStmtNode(String variable, ExprNode expression) {
        this.variable = variable;
        this.expression = expression;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
