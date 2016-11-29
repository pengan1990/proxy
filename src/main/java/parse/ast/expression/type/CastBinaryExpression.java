/**
 * (created at 2011-1-20)
 */
package parse.ast.expression.type;

import parse.ast.expression.Expression;
import parse.ast.expression.UnaryOperatorExpression;

/**
 * <code>'BINARY' higherExpr</code>
 */
public class CastBinaryExpression extends UnaryOperatorExpression {
    public CastBinaryExpression(Expression operand) {
        super(operand, PRECEDENCE_BINARY);
    }

    @Override
    public String getOperator() {
        return "BINARY";
    }

}
