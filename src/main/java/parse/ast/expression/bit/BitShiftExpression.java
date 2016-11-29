/**
 * (created at 2011-1-20)
 */
package parse.ast.expression.bit;

import parse.ast.expression.BinaryOperatorExpression;
import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

/**
 * <code>higherExpr ( ('&lt;&lt;'|'&gt;&gt;') higherExpr )+</code>
 */
public class BitShiftExpression extends BinaryOperatorExpression {
    private final boolean negative;

    /**
     * @param negative true if right shift
     */
    public BitShiftExpression(boolean negative, Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_BIT_SHIFT);
        this.negative = negative;
    }

    public boolean isRightShift() {
        return negative;
    }

    @Override
    public String getOperator() {
        return negative ? ">>" : "<<";
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
