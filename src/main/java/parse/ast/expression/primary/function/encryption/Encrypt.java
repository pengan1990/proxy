/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.encryption;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class Encrypt extends FunctionExpression {
    public Encrypt(List<Expression> arguments) {
        super("ENCRYPT", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Encrypt(arguments);
    }

}
