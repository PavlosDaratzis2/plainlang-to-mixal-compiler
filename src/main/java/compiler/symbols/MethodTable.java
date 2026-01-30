// Method table
package compiler.symbols;

import java.util.HashMap;
import java.util.Map;

public class MethodTable {
    private final Map<String, MethodSignature> methods = new HashMap<>();

    public boolean declare(String name, MethodSignature sig) {
        if (methods.containsKey(name)) return false;
        methods.put(name, sig);
        return true;
    }

    public MethodSignature lookup(String name) {
        return methods.get(name);
    }

    public boolean hasMain() {
        return methods.containsKey("main") && methods.get("main").paramCount == 0;
    }
}
