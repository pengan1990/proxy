/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.info;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class Charset extends FunctionExpression {
    public Charset(List<Expression> arguments) {
        super("CHARSET", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Charset(arguments);
    }

}
