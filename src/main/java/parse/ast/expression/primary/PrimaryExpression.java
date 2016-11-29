/**
 * (created at 2011-4-12)
 */
package parse.ast.expression.primary;

import parse.ast.expression.AbstractExpression;

import java.util.Map;

/**

 */
public abstract class PrimaryExpression extends AbstractExpression {
    @Override
    public int getPrecedence() {
        return PRECEDENCE_PRIMARY;
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return UNEVALUATABLE;
    }
}
