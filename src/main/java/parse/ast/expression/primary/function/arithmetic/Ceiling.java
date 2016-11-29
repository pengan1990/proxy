/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.arithmetic;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class Ceiling extends FunctionExpression {
    public Ceiling(List<Expression> arguments) {
        super("CEILING", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Ceiling(arguments);
    }

}
