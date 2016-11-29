/**
 * (created at 2011-1-19)
 */
package parse.ast.expression.comparison;

import parse.ast.expression.AbstractExpression;
import parse.ast.expression.Expression;
import parse.ast.expression.ReplacableExpression;
import parse.visitor.SQLASTVisitor;

import java.util.Map;

/**

 */
public class ComparisionIsExpression extends AbstractExpression implements ReplacableExpression {
    public static final int IS_NULL = 1;
    public static final int IS_TRUE = 2;
    public static final int IS_FALSE = 3;
    public static final int IS_UNKNOWN = 4;
    public static final int IS_NOT_NULL = 5;
    public static final int IS_NOT_TRUE = 6;
    public static final int IS_NOT_FALSE = 7;
    public static final int IS_NOT_UNKNOWN = 8;

    private final int mode;
    private final Expression operand;
    private Expression replaceExpr;

    /**
     * @param mode {@link #IS_NULL} or {@link #IS_TRUE} or {@link #IS_FALSE} or
     *             {@link #IS_UNKNOWN} or {@link #IS_NOT_NULL} or
     *             {@link #IS_NOT_TRUE} or {@link #IS_NOT_FALSE} or
     *             {@link #IS_NOT_UNKNOWN}
     */
    public ComparisionIsExpression(Expression operand, int mode) {
        this.operand = operand;
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public Expression getOperand() {
        return operand;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_COMPARISION;
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return UNEVALUATABLE;
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
