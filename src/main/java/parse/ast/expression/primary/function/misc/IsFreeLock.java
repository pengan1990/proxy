/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.misc;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class IsFreeLock extends FunctionExpression {
    public IsFreeLock(List<Expression> arguments) {
        super("IS_FREE_LOCK", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new IsFreeLock(arguments);
    }

}
