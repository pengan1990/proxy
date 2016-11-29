/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.misc;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class UuidShort extends FunctionExpression {
    public UuidShort(List<Expression> arguments) {
        super("UUID_SHORT", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new UuidShort(arguments);
    }

}
