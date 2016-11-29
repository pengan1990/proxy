/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.string;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class Ltrim extends FunctionExpression {
    public Ltrim(List<Expression> arguments) {
        super("LTRIM", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Ltrim(arguments);
    }

}
