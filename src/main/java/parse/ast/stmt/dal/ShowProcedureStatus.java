/**
 * (created at 2011-5-20)
 */
package parse.ast.stmt.dal;

import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowProcedureStatus extends DALShowStatement {
    private final String pattern;
    private final Expression where;

    public ShowProcedureStatus(String pattern) {
        this.pattern = pattern;
        this.where = null;
    }

    public ShowProcedureStatus(Expression where) {
        this.pattern = null;
        this.where = where;
    }

    public ShowProcedureStatus() {
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
