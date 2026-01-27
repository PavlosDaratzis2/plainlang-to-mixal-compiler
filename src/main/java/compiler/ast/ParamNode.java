/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.ast;

public class ParamNode extends AstNode {
    public final String type;
    public final String name;

    public ParamNode(String type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return null;
    }
}
