/**
 * (created at 2011-1-20)
 */
package parse.ast.expression.type;

import parse.ast.expression.AbstractExpression;
import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

import java.util.Map;

/**
 * <code>higherExpr 'COLLATE' collateName</code>
 *

 */
public class CollateExpression extends AbstractExpression {
    private final String collateName;
    private final Expression string;

    public CollateExpression(Expression string, String collateName) {
        if (collateName == null) throw new IllegalArgumentException("collateName is null");
        this.string = string;
        this.collateName = collateName;
    }

    public String getCollateName() {
        return collateName;
    }

    public Expression getString() {
        return string;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_COLLATE;
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return string.evaluation(parameters);
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
