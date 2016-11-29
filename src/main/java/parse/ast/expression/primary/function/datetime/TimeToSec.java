/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.datetime;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class TimeToSec extends FunctionExpression {
    public TimeToSec(List<Expression> arguments) {
        super("TIME_TO_SEC", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new TimeToSec(arguments);
    }

}
