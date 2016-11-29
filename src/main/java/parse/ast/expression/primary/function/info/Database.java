/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.info;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class Database extends FunctionExpression {
    public Database(List<Expression> arguments) {
        super("DATABASE", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Database(arguments);
    }

}
