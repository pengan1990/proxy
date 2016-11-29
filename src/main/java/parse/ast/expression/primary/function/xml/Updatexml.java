/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.xml;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class Updatexml extends FunctionExpression {
    public Updatexml(List<Expression> arguments) {
        super("UPDATEXML", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Updatexml(arguments);
    }

}
