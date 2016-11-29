/**
 * (created at 2011-1-19)
 */
package parse.ast.expression.string;

import parse.ast.expression.BinaryOperatorExpression;
import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

/**
 * <code>higherPreExpr 'SOUNDS' 'LIKE' higherPreExpr</code>
 */
public class SoundsLikeExpression extends BinaryOperatorExpression {
    public SoundsLikeExpression(Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_COMPARISION);
    }

    @Override
    public String getOperator() {
        return "SOUNDS LIKE";
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
