/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.flowctrl;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class Nullif extends FunctionExpression {
    public Nullif(List<Expression> arguments) {
        super("NULLIF", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Nullif(arguments);
    }

}
