/**
 * (created at 2011-1-18)
 */
package parse.ast.expression.comparison;

import parse.ast.expression.Expression;
import parse.ast.expression.ReplacableExpression;
import parse.ast.expression.TernaryOperatorExpression;
import parse.visitor.SQLASTVisitor;

/**
 */
public class BetweenAndExpression extends TernaryOperatorExpression implements ReplacableExpression {
    private final boolean not;
    private Expression replaceExpr;

    public BetweenAndExpression(boolean not, Expression comparee, Expression notLessThan, Expression notGreaterThan) {
        super(comparee, notLessThan, notGreaterThan);
        this.not = not;
    }

    public boolean isNot() {
        return not;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_BETWEEN_AND;
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
