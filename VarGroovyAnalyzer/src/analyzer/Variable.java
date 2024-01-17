package analyzer;

public class Variable {
    public static final int UNUSED = 0;
    public static final int USED = 1;
    public static final int MODIFIED = 2;
    public static final int CONTROLLED = 4;
    public static final int INPUT = 8;
    public static final int OUTPUT = 16;
    public int flags;
    public int spen;
    public final String token;

    public Variable(String token) {
        this(token, UNUSED);
    }
    public Variable(String token, int f) {
        this.token = token;
        this.flags = f;
    }

    public void addFlag(int f) {
        flags |= f;
    }
    public void removeFlag(int f) {
        flags &= ~f;
    }
    public boolean hasFlag(int f) {
        return (flags & f) != 0;
    }
}