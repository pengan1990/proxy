/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.datetime;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class PeriodAdd extends FunctionExpression {
    public PeriodAdd(List<Expression> arguments) {
        super("PERIOD_ADD", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new PeriodAdd(arguments);
    }

}
