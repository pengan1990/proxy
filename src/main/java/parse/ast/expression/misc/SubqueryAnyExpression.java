/**
 * (created at 2011-1-20)
 */
package parse.ast.expression.misc;

import parse.ast.expression.UnaryOperatorExpression;

/**
 * <code>('ANY'|'SOME') '(' subquery ')'</code>
 */
public class SubqueryAnyExpression extends UnaryOperatorExpression {
    public SubqueryAnyExpression(QueryExpression subquery) {
        super(subquery, PRECEDENCE_PRIMARY);
    }

    @Override
    public String getOperator() {
        return "ANY";
    }

}
