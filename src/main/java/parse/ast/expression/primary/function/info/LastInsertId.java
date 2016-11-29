/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.info;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class LastInsertId extends FunctionExpression {
    public LastInsertId(List<Expression> arguments) {
        super("LAST_INSERT_ID", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new LastInsertId(arguments);
    }

}
