/**
 * (created at 2011-1-19)
 */
package parse.ast.expression.string;

import parse.ast.expression.Expression;
import parse.ast.expression.TernaryOperatorExpression;
import parse.visitor.SQLASTVisitor;

/**
 * <code>higherPreExpr 'NOT'? 'LIKE' higherPreExpr ('ESCAPE' higherPreExpr)?</code>
 */
public class LikeExpression extends TernaryOperatorExpression {
    private final boolean not;

    /**
     * @param escape null is no ESCAPE
     */
    public LikeExpression(boolean not, Expression comparee, Expression pattern, Expression escape) {
        super(comparee, pattern, escape);
        this.not = not;
    }

    public boolean isNot() {
        return not;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_COMPARISION;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
