/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.datetime;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class Weekofyear extends FunctionExpression {
    public Weekofyear(List<Expression> arguments) {
        super("WEEKOFYEAR", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Weekofyear(arguments);
    }

}
