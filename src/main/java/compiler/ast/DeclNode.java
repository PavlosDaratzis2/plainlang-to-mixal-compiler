package compiler.ast;

public class DeclNode extends StmtNode {
    public final String type;
    public final String name;
    public final ExprNode initialValue; // μπορεί να είναι null

    public DeclNode(String type, String name, ExprNode initialValue) {
        this.type = type;
        this.name = name;
        this.initialValue = initialValue;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}