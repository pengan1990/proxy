/**
 * (created at 2011-5-21)
 */
package parse.ast.stmt.dal;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.Identifier;
import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowTables extends DALShowStatement {
    private final boolean full;
    private final String pattern;
    private final Expression where;
    private Identifier schema;

    public ShowTables(boolean full, Identifier schema, String pattern) {
        this.full = full;
        this.schema = schema;
        this.pattern = pattern;
        this.where = null;
    }

    public ShowTables(boolean full, Identifier schema, Expression where) {
        this.full = full;
        this.schema = schema;
        this.pattern = null;
        this.where = where;
    }

    public ShowTables(boolean full, Identifier schema) {
        this.full = full;
        this.schema = schema;
        this.pattern = null;
        this.where = null;
    }

    public boolean isFull() {
        return full;
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
