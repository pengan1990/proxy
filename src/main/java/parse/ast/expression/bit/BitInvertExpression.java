/**
 * (created at 2011-1-20)
 */
package parse.ast.expression.bit;

import parse.ast.expression.Expression;
import parse.ast.expression.UnaryOperatorExpression;

/**
 * <code>'~' higherExpr</code>
 */
public class BitInvertExpression extends UnaryOperatorExpression {
    public BitInvertExpression(Expression operand) {
        super(operand, PRECEDENCE_UNARY_OP);
    }

    @Override
    public String getOperator() {
        return "~";
    }

}
