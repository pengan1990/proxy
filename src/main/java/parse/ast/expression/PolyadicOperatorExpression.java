/**
 * (created at 2012-8-2)
 */
package parse.ast.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * an operator with arity of n<br/>
 * associative and commutative<br/>
 * non-polyadic operator with same precedence is not exist
 */
public abstract class PolyadicOperatorExpression extends AbstractExpression {
    protected final int precedence;
    protected List<Expression> operands;

    public PolyadicOperatorExpression(int precedence) {
        this(precedence, true);
    }

    public PolyadicOperatorExpression(int precedence, boolean leftCombine) {
        this(precedence, 4);
    }

    public PolyadicOperatorExpression(int precedence, int initArity) {
        this.precedence = precedence;
        this.operands = new ArrayList<Expression>(initArity);
    }

    /**
     * @return this
     */
    public PolyadicOperatorExpression appendOperand(Expression operand) {
        if (operand == null) return this;
        if (getClass().isAssignableFrom(operand.getClass())) {
            PolyadicOperatorExpression sub = (PolyadicOperatorExpression) operand;
            operands.addAll(sub.operands);
        } else {
            operands.add(operand);
        }
        return this;
    }

    /**
     * @param index start from 0
     */
    public Expression getOperand(int index) {
        if (index >= operands.size()) {
            throw new IllegalArgumentException("only contains "
                    + operands.size()
                    + " operands,"
                    + index
                    + " is out of bound");
        }
        return operands.get(index);
    }

    public int getArity() {
        return operands.size();
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }

    public abstract String getOperator();

    @Override
    protected Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return UNEVALUATABLE;
    }
}
