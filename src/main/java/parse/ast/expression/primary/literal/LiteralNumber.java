/**
 * (created at 2011-1-21)
 */
package parse.ast.expression.primary.literal;

import parse.visitor.SQLASTVisitor;

import java.util.Map;

/**
 * literal date is also possible
 */
public class LiteralNumber extends Literal {
    private final Number number;

    public LiteralNumber(Number number) {
        super();
        if (number == null) throw new IllegalArgumentException("number is null!");
        this.number = number;
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return number;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public Number getNumber() {
        return number;
    }

}
