/**
 * (created at 2011-1-12)
 */
package parse.ast.expression.logical;

import parse.ast.expression.Expression;
import parse.ast.expression.PolyadicOperatorExpression;
import parse.ast.expression.primary.literal.LiteralBoolean;
import parse.util.ExprEvalUtils;
import parse.visitor.SQLASTVisitor;

import java.util.Map;

/**

 */
public class LogicalOrExpression extends PolyadicOperatorExpression {
    public LogicalOrExpression() {
        super(PRECEDENCE_LOGICAL_OR);
    }

    @Override
    public String getOperator() {
        return "OR";
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        for (Expression operand : operands) {
            Object val = operand.evaluation(parameters);
            if (val == null) return null;
            if (val == UNEVALUATABLE) return UNEVALUATABLE;
            if (ExprEvalUtils.obj2bool(val)) {
                return LiteralBoolean.TRUE;
            }
        }
        return LiteralBoolean.FALSE;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
