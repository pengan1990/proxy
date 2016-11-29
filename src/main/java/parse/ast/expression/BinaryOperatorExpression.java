/**
 * (created at 2011-4-13)
 */
package parse.ast.expression;

import java.util.Map;

/**
 * an operator with arity of 3<br/>
 * left conbine in default
 */
public abstract class BinaryOperatorExpression extends AbstractExpression {
    protected final Expression leftOprand;
    protected final Expression rightOprand;
    protected final int precedence;
    protected final boolean leftCombine;

    /**
     * {@link #leftCombine} is true
     */
    protected BinaryOperatorExpression(Expression leftOprand, Expression rightOprand, int precedence) {
        this.leftOprand = leftOprand;
        this.rightOprand = rightOprand;
        this.precedence = precedence;
        this.leftCombine = true;
    }

    protected BinaryOperatorExpression(Expression leftOprand, Expression rightOprand, int precedence,
                                       boolean leftCombine) {
        this.leftOprand = leftOprand;
        this.rightOprand = rightOprand;
        this.precedence = precedence;
        this.leftCombine = leftCombine;
    }

    public Expression getLeftOprand() {
        return leftOprand;
    }

    public Expression getRightOprand() {
        return rightOprand;
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }

    public boolean isLeftCombine() {
        return leftCombine;
    }

    public abstract String getOperator();

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return UNEVALUATABLE;
    }

}
