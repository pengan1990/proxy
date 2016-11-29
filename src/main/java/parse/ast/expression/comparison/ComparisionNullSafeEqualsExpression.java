/**
 * (created at 2011-1-19)
 */
package parse.ast.expression.comparison;

import parse.ast.expression.BinaryOperatorExpression;
import parse.ast.expression.Expression;
import parse.ast.expression.ReplacableExpression;
import parse.ast.expression.primary.literal.LiteralBoolean;
import parse.util.ExprEvalUtils;
import parse.util.Pair;
import parse.visitor.SQLASTVisitor;

import java.util.Map;

/**
 * <code>higherPreExpr '<=>' higherPreExpr</code>
 */
public class ComparisionNullSafeEqualsExpression extends BinaryOperatorExpression implements ReplacableExpression {
    private Expression replaceExpr;

    public ComparisionNullSafeEqualsExpression(Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_COMPARISION);
    }

    @Override
    public String getOperator() {
        return "<=>";
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        Object left = leftOprand.evaluation(parameters);
        Object right = rightOprand.evaluation(parameters);
        if (left == UNEVALUATABLE || right == UNEVALUATABLE) return UNEVALUATABLE;
        if (left == null) return right == null ? LiteralBoolean.TRUE : LiteralBoolean.FALSE;
        if (right == null) return LiteralBoolean.FALSE;
        if (left instanceof Number || right instanceof Number) {
            Pair<Number, Number> pair = ExprEvalUtils.convertNum2SameLevel(left, right);
            left = pair.getKey();
            right = pair.getValue();
        }
        return left.equals(right) ? LiteralBoolean.TRUE : LiteralBoolean.FALSE;
    }

    @Override
    public void setReplaceExpr(Expression replaceExpr) {
        this.replaceExpr = replaceExpr;
    }

    @Override
    public void clearReplaceExpr() {
        this.replaceExpr = null;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        if (replaceExpr == null) visitor.visit(this);
        else replaceExpr.accept(visitor);
    }
}
