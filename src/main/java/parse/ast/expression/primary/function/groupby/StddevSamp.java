/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.groupby;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class StddevSamp extends FunctionExpression {
    public StddevSamp(List<Expression> arguments) {
        super("STDDEV_SAMP", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new StddevSamp(arguments);
    }

}
