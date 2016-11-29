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
public class LogicalAndExpression extends PolyadicOperatorExpression {
    public LogicalAndExpression() {
        super(PRECEDENCE_LOGICAL_AND);
    }

    @Override
    public String getOperator() {
        return "AND";
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        for (Expression operand : operands) {
            Object val = operand.evaluation(parameters);
            if (val == null) return null;
            if (val == UNEVALUATABLE) return UNEVALUATABLE;
            if (!ExprEvalUtils.obj2bool(val)) {
                return LiteralBoolean.FALSE;
            }
        }
        return LiteralBoolean.TRUE;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
