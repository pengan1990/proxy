/**
 * (created at 2011-5-21)
 */
package parse.ast.stmt.dal;

import parse.ast.fragment.Limit;
import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowWarnings extends DALShowStatement {
    private final boolean count;
    private final Limit limit;

    public ShowWarnings(boolean count, Limit limit) {
        this.count = count;
        this.limit = limit;
    }

    public boolean isCount() {
        return count;
    }

    public Limit getLimit() {
        return limit;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
