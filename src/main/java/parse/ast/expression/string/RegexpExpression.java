/**
 * (created at 2011-1-19)
 */
package parse.ast.expression.string;

import parse.ast.expression.BinaryOperatorExpression;
import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

/**
 * <code>higherPreExpr 'NOT'? ('REGEXP'|'RLIKE') higherPreExp</code>
 */
public class RegexpExpression extends BinaryOperatorExpression {
    private final boolean not;

    public RegexpExpression(boolean not, Expression comparee, Expression pattern) {
        super(comparee, pattern, PRECEDENCE_COMPARISION);
        this.not = not;
    }

    public boolean isNot() {
        return not;
    }

    @Override
    public String getOperator() {
        return not ? "NOT REGEXP" : "REGEXP";
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
