/**
 * (created at 2011-5-21)
 */
package parse.ast.stmt.dal;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.Identifier;
import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowOpenTables extends DALShowStatement {
    private final String pattern;
    private final Expression where;
    private Identifier schema;

    public ShowOpenTables(Identifier schema, String pattern) {
        this.schema = schema;
        this.pattern = pattern;
        this.where = null;
    }

    public ShowOpenTables(Identifier schema, Expression where) {
        this.schema = schema;
        this.pattern = null;
        this.where = where;
    }

    public ShowOpenTables(Identifier schema) {
        this.schema = schema;
        this.pattern = null;
        this.where = null;
    }

    public Identifier getSchema() {
        return schema;
    }

    public void setSchema(Identifier schema) {
        this.schema = schema;
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
