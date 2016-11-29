/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.encryption;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class Sha2 extends FunctionExpression {
    public Sha2(List<Expression> arguments) {
        super("SHA2", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Sha2(arguments);
    }

}
