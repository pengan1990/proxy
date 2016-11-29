/**
 * (created at 2011-2-9)
 */
package parse.ast.fragment.tableref;

import parse.ast.expression.misc.QueryExpression;
import parse.visitor.SQLASTVisitor;

/**

 */
public class SubqueryFactor extends AliasableTableReference {
    private final QueryExpression subquery;

    public SubqueryFactor(QueryExpression subquery, String alias) {
        super(alias);
        if (alias == null) throw new IllegalArgumentException("alias is required for subquery factor");
        if (subquery == null) throw new IllegalArgumentException("subquery is null");
        this.subquery = subquery;
    }

    public QueryExpression getSubquery() {
        return subquery;
    }

    @Override
    public Object removeLastConditionElement() {
        return null;
    }

    @Override
    public boolean isSingleTable() {
        return false;
    }

    @Override
    public int getPrecedence() {
        return TableReference.PRECEDENCE_FACTOR;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
