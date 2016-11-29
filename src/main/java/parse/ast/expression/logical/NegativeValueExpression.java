/**
 * (created at 2011-1-20)
 */
package parse.ast.expression.logical;

import parse.ast.expression.Expression;
import parse.ast.expression.UnaryOperatorExpression;
import parse.ast.expression.primary.literal.LiteralBoolean;
import parse.util.ExprEvalUtils;

import java.util.Map;

/**
 * <code>'!' higherExpr</code>
 */
public class NegativeValueExpression extends UnaryOperatorExpression {
    public NegativeValueExpression(Expression operand) {
        super(operand, PRECEDENCE_UNARY_OP);
    }

    @Override
    public String getOperator() {
        return "!";
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        Object operand = getOperand().evaluation(parameters);
        if (operand == null) return null;
        if (operand == UNEVALUATABLE) return UNEVALUATABLE;
        boolean bool = ExprEvalUtils.obj2bool(operand);
        return bool ? LiteralBoolean.FALSE : LiteralBoolean.TRUE;
    }

}
