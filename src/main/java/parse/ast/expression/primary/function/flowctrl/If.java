/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.flowctrl;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class If extends FunctionExpression {
    public If(List<Expression> arguments) {
        super("IF", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new If(arguments);
    }

}
