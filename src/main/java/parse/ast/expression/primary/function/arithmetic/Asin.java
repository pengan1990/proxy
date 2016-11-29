/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.arithmetic;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class Asin extends FunctionExpression {
    public Asin(List<Expression> arguments) {
        super("ASIN", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Asin(arguments);
    }

}
