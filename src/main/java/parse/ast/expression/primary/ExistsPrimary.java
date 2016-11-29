/**
 * (created at 2011-1-21)
 */
package parse.ast.expression.primary;

import parse.ast.expression.misc.QueryExpression;
import parse.visitor.SQLASTVisitor;

/**
 * <code>'EXISTS' '(' subquery ')'</code>
 */
public class ExistsPrimary extends PrimaryExpression {
    private final QueryExpression subquery;

    public ExistsPrimary(QueryExpression subquery) {
        if (subquery == null) throw new IllegalArgumentException("subquery is null for EXISTS expression");
        this.subquery = subquery;
    }

    /**
     * @return never null
     */
    public QueryExpression getSubquery() {
        return subquery;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
