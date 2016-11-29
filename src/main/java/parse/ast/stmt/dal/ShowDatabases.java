/**
 * (created at 2011-5-20)
 */
package parse.ast.stmt.dal;

import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowDatabases extends DALShowStatement {
    private final String pattern;
    private final Expression where;

    public ShowDatabases(String pattern) {
        super();
        this.pattern = pattern;
        this.where = null;
        stmtType = StmtType.DAL_SHOW_DATABASES;
    }

    public ShowDatabases(Expression where) {
        super();
        this.pattern = null;
        this.where = where;
        stmtType = StmtType.DAL_SHOW_DATABASES;
    }

    public ShowDatabases() {
        super();
        this.pattern = null;
        this.where = null;
        stmtType = StmtType.DAL_SHOW_DATABASES;
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
