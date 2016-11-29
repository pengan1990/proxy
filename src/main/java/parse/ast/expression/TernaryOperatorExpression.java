/**
 * (created at 2011-1-18)
 */
package parse.ast.expression;

import java.util.Map;

/**
 * an operator with arity of 3
 */
public abstract class TernaryOperatorExpression extends AbstractExpression {
    private final Expression first;
    private final Expression second;
    private final Expression third;

    public TernaryOperatorExpression(Expression first, Expression second, Expression third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public Expression getFirst() {
        return first;
    }

    public Expression getSecond() {
        return second;
    }

    public Expression getThird() {
        return third;
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return UNEVALUATABLE;
    }

}
