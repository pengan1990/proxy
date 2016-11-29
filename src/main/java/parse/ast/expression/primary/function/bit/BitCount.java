/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.bit;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class BitCount extends FunctionExpression {
    public BitCount(List<Expression> arguments) {
        super("BIT_COUNT", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new BitCount(arguments);
    }

}
