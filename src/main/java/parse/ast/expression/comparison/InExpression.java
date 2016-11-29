/**
 * (created at 2011-1-19)
 */
package parse.ast.expression.comparison;

import parse.ast.expression.BinaryOperatorExpression;
import parse.ast.expression.Expression;
import parse.ast.expression.ReplacableExpression;
import parse.ast.expression.misc.InExpressionList;
import parse.ast.expression.misc.QueryExpression;
import parse.visitor.SQLASTVisitor;

/**
 * <code>higherPreExpr (NOT)? IN ( '(' expr (',' expr)* ')' | subquery )</code>
 */
public class InExpression extends BinaryOperatorExpression implements ReplacableExpression {
    private final boolean not;
    private Expression replaceExpr;

    /**
     * @param rightOprand {@link QueryExpression} or {@link InExpressionList}
     */
    public InExpression(boolean not, Expression leftOprand, Expression rightOprand) {
        super(leftOprand, rightOprand, PRECEDENCE_COMPARISION);
        this.not = not;
    }

    public boolean isNot() {
        return not;
    }

    public InExpressionList getInExpressionList() {
        if (rightOprand instanceof InExpressionList) {
            return (InExpressionList) rightOprand;
        }
        return null;
    }

    public QueryExpression getQueryExpression() {
        if (rightOprand instanceof QueryExpression) {
            return (QueryExpression) rightOprand;
        }
        return null;
    }

    @Override
    public String getOperator() {
        return not ? "NOT IN" : "IN";
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
