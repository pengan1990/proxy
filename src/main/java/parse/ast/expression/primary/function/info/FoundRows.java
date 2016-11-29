/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.info;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class FoundRows extends FunctionExpression {
    public FoundRows(List<Expression> arguments) {
        super("FOUND_ROWS", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new FoundRows(arguments);
    }

}
