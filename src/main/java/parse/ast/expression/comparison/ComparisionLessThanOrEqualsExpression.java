/**
 * (created at 2011-1-19)
 */
package parse.ast.expression.comparison;

import parse.ast.expression.BinaryOperatorExpression;
import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

/**
 * <code>higherPreExpr '<=' higherPreExpr</code>
 */
public class ComparisionLessThanOrEqualsExpression extends BinaryOperatorExpression {
    public ComparisionLessThanOrEqualsExpression(Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_COMPARISION);
    }

    @Override
    public String getOperator() {
        return "<=";
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
