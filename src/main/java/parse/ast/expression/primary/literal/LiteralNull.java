/**
 * (created at 2011-1-21)
 */
package parse.ast.expression.primary.literal;

import parse.visitor.SQLASTVisitor;

import java.util.Map;

/**

 */
public class LiteralNull extends Literal {
    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return null;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
