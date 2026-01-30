// Symbol table
package compiler.symbols;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, String> table = new HashMap<>();

    public boolean declare(String name, String type) {
        if (table.containsKey(name)) return false;
        table.put(name, type);
        return true;
    }

    public String lookup(String name) {
        return table.get(name);
    }
}
