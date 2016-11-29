/**
 * (created at 2011-1-21)
 */
package parse.ast.expression.primary.literal;

import parse.visitor.SQLASTVisitor;

import java.util.Map;

/**

 */
public class LiteralBoolean extends Literal {
    public static final Integer TRUE = new Integer(1);
    public static final Integer FALSE = new Integer(0);
    private final boolean value;

    public LiteralBoolean(boolean value) {
        super();
        this.value = value;
    }

    public boolean isTrue() {
        return value;
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return value ? TRUE : FALSE;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
