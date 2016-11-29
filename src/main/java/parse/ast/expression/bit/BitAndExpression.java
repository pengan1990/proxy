/**
 * (created at 2011-1-20)
 */
package parse.ast.expression.bit;

import parse.ast.expression.BinaryOperatorExpression;
import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

/**
 * <code>higherExpr '&' higherExpr</code>
 */
public class BitAndExpression extends BinaryOperatorExpression {
    public BitAndExpression(Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_BIT_AND);
    }

    @Override
    public String getOperator() {
        return "&";
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
