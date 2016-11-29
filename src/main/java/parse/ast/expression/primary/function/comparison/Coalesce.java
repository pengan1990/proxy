/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.comparison;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class Coalesce extends FunctionExpression {
    public Coalesce(List<Expression> arguments) {
        super("COALESCE", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Coalesce(arguments);
    }

}
