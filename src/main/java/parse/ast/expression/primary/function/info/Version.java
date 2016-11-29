/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.info;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class Version extends FunctionExpression {
    public Version(List<Expression> arguments) {
        super("VERSION", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Version(arguments);
    }

}
