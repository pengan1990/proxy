/**
 * (created at 2011-5-20)
 */
package parse.ast.stmt.dal;

import parse.ast.expression.primary.Identifier;
import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowCreate extends DALShowStatement {
    private final Type type;
    private final Identifier id;
    public ShowCreate(Type type, Identifier id) {
        this.type = type;
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public Identifier getId() {
        return id;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * enum name must equals to real sql string
     */
    public static enum Type {
        DATABASE,
        EVENT,
        FUNCTION,
        PROCEDURE,
        TABLE,
        TRIGGER,
        VIEW
    }
}
