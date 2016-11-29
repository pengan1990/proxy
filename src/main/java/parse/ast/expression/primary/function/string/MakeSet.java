/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.string;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class MakeSet extends FunctionExpression {
    public MakeSet(List<Expression> arguments) {
        super("MAKE_SET", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new MakeSet(arguments);
    }

}
