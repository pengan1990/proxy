/**
 * (created at 2011-1-12)
 */
package parse.ast.expression.logical;

import parse.ast.expression.BinaryOperatorExpression;
import parse.ast.expression.Expression;
import parse.ast.expression.primary.literal.LiteralBoolean;
import parse.util.ExprEvalUtils;
import parse.visitor.SQLASTVisitor;

import java.util.Map;

/**

 */
public class LogicalXORExpression extends BinaryOperatorExpression {
    public LogicalXORExpression(Expression left, Expression right) {
        super(left, right, PRECEDENCE_LOGICAL_XOR);
    }

    @Override
    public String getOperator() {
        return "XOR";
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        Object left = leftOprand.evaluation(parameters);
        Object right = rightOprand.evaluation(parameters);
        if (left == null || right == null) return null;
        if (left == UNEVALUATABLE || right == UNEVALUATABLE) return UNEVALUATABLE;
        boolean b1 = ExprEvalUtils.obj2bool(left);
        boolean b2 = ExprEvalUtils.obj2bool(right);
        return b1 != b2 ? LiteralBoolean.TRUE : LiteralBoolean.FALSE;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
