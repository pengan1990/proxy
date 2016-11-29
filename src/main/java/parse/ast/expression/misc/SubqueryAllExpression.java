/**
 * (created at 2011-1-20)
 */
package parse.ast.expression.misc;

import parse.ast.expression.UnaryOperatorExpression;

/**
 * <code>'ALL' '(' subquery  ')'</code>
 */
public class SubqueryAllExpression extends UnaryOperatorExpression {
    public SubqueryAllExpression(QueryExpression subquery) {
        super(subquery, PRECEDENCE_PRIMARY);
    }

    @Override
    public String getOperator() {
        return "ALL";
    }

}
