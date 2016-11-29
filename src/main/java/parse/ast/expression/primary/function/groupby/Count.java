/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.groupby;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;
import parse.visitor.SQLASTVisitor;

import java.util.List;

/**

 */
public class Count extends FunctionExpression {
    /**
     * either {@link distinct} or {@link wildcard} is false. if both are false,
     * expressionList must be size 1
     */
    private final boolean distinct;

    public Count(List<Expression> arguments) {
        super("COUNT", arguments);
        this.distinct = true;
    }

    public Count(Expression arg) {
        super("COUNT", wrapList(arg));
        this.distinct = false;
    }

    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Count(arguments);
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
