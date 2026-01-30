// Method signature
package compiler.symbols;

public class MethodSignature {
    public final String returnType;
    public final int paramCount;

    public MethodSignature(String returnType, int paramCount) {
        this.returnType = returnType;
        this.paramCount = paramCount;
    }

    public int paramCount() {
        return paramCount;
    }
}
