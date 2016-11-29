/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.datetime;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class ConvertTz extends FunctionExpression {
    public ConvertTz(List<Expression> arguments) {
        super("CONVERT_TZ", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new ConvertTz(arguments);
    }

}
