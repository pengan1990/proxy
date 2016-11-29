/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.datetime;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class ToSeconds extends FunctionExpression {
    public ToSeconds(List<Expression> arguments) {
        super("TO_SECONDS", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new ToSeconds(arguments);
    }

}
