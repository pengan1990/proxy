/**
 * (created at 2011-1-14)
 */
package parse.ast.expression;

import parse.visitor.SQLASTVisitor;

import java.util.Map;

/**

 */
public abstract class UnaryOperatorExpression extends AbstractExpression {
    protected final int precedence;
    private final Expression operand;

    public UnaryOperatorExpression(Expression operand, int precedence) {
        if (operand == null) throw new IllegalArgumentException("operand is null");
        this.operand = operand;
        this.precedence = precedence;
    }

    public Expression getOperand() {
        return operand;
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }

    public abstract String getOperator();

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return UNEVALUATABLE;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
