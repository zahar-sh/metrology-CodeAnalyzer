package analyzer;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Block {
    public final Block parent;
    public final String start;
    public String end;
    public final ArrayList<Block> children = new ArrayList<>();
    public final ArrayList<Variable> vars = new ArrayList<>();

    public Block(Block parent, String start) {
        this.parent = parent;
        this.start = start;
    }

    public Variable varOf(String token) {
        for (Variable variable : vars)
            if (token.equals(variable.token))
                return variable;
        return null;
    }
    public Block hasVar(String token) {
        return varOf(token) == null ? (parent == null ? null : parent.hasVar(token)) : this;
    }
    public void forEachVar(Consumer<Variable> consumer) {
        vars.forEach(consumer);
        for (Block child : children)
            child.forEachVar(consumer);
    }
}
