/**
 * (created at 2011-5-20)
 */
package parse.ast.stmt.dal;

import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowEngine extends DALShowStatement {
    private final Type type;

    public ShowEngine(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public static enum Type {
        INNODB_STATUS,
        INNODB_MUTEX,
        PERFORMANCE_SCHEMA_STATUS
    }
}
