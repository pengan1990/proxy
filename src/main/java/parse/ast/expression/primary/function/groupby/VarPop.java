/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.groupby;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class VarPop extends FunctionExpression {
    public VarPop(List<Expression> arguments) {
        super("VAR_POP", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new VarPop(arguments);
    }

}
