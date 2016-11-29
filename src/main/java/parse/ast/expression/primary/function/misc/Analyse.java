/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.misc;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**
 * MySQL extending function
 */
public class Analyse extends FunctionExpression {
    public Analyse(List<Expression> arguments) {
        super("ANALYSE", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Analyse(arguments);
    }

}
