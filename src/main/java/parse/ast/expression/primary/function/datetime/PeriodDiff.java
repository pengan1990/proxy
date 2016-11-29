/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.datetime;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class PeriodDiff extends FunctionExpression {
    public PeriodDiff(List<Expression> arguments) {
        super("PERIOD_DIFF", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new PeriodDiff(arguments);
    }

}
