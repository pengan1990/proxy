/**
 * (created at 2011-7-20)
 */
package parse.ast.expression.arithmeic;

import parse.ast.expression.BinaryOperatorExpression;
import parse.ast.expression.Expression;
import parse.util.BinaryOperandCalculator;
import parse.util.ExprEvalUtils;
import parse.util.Pair;

import java.util.Map;

/**
 */
public abstract class ArithmeticBinaryOperatorExpression extends BinaryOperatorExpression
        implements BinaryOperandCalculator {
    protected ArithmeticBinaryOperatorExpression(Expression leftOprand, Expression rightOprand, int precedence) {
        super(leftOprand, rightOprand, precedence, true);
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        Object left = leftOprand.evaluation(parameters);
        Object right = rightOprand.evaluation(parameters);
        if (left == null || right == null) return null;
        if (left == UNEVALUATABLE || right == UNEVALUATABLE) return UNEVALUATABLE;
        Pair<Number, Number> pair = ExprEvalUtils.convertNum2SameLevel(left, right);
        return ExprEvalUtils.calculate(this, pair.getKey(), pair.getValue());
    }

}
