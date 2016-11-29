/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.info;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class ConnectionId extends FunctionExpression {
    public ConnectionId(List<Expression> arguments) {
        super("CONNECTION_ID", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new ConnectionId(arguments);
    }

}
