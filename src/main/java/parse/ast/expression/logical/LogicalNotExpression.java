/**
 * (created at 2011-1-12)
 */
package parse.ast.expression.logical;

import parse.ast.expression.Expression;
import parse.ast.expression.UnaryOperatorExpression;
import parse.ast.expression.primary.literal.LiteralBoolean;
import parse.util.ExprEvalUtils;

import java.util.Map;

/**
 * <code>'NOT' higherExpr</code>
 */
public class LogicalNotExpression extends UnaryOperatorExpression {
    public LogicalNotExpression(Expression operand) {
        super(operand, PRECEDENCE_LOGICAL_NOT);
    }

    @Override
    public String getOperator() {
        return "NOT";
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
