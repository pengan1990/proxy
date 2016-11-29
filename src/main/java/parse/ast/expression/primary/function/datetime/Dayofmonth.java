/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.datetime;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class Dayofmonth extends FunctionExpression {
    public Dayofmonth(List<Expression> arguments) {
        super("DAYOFMONTH", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Dayofmonth(arguments);
    }

}
