/**
 * (created at 2011-5-20)
 */
package parse.ast.stmt.dal;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.Identifier;
import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowTableStatus extends DALShowStatement {
    private final String pattern;
    private final Expression where;
    private Identifier database;

    public ShowTableStatus(Identifier database, Expression where) {
        this.database = database;
        this.pattern = null;
        this.where = where;
    }

    public ShowTableStatus(Identifier database, String pattern) {
        this.database = database;
        this.pattern = pattern;
        this.where = null;
    }

    public ShowTableStatus(Identifier database) {
        this.database = database;
        this.pattern = null;
        this.where = null;
    }

    public Identifier getDatabase() {
        return database;
    }

    public void setDatabase(Identifier database) {
        this.database = database;
    }

    public String getPattern() {
        return pattern;
    }

    public Expression getWhere() {
        return where;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
