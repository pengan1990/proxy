/**
 * (created at 2011-5-20)
 */
package parse.ast.stmt.dal;

import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowCollation extends DALShowStatement {
    private final String pattern;
    private final Expression where;

    public ShowCollation(Expression where) {
        this.pattern = null;
        this.where = where;
    }

    public ShowCollation(String pattern) {
        this.pattern = pattern;
        this.where = null;
    }

    public ShowCollation() {
        this.pattern = null;
        this.where = null;
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
