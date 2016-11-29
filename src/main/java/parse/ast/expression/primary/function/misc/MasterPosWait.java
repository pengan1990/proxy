/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.misc;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class MasterPosWait extends FunctionExpression {
    public MasterPosWait(List<Expression> arguments) {
        super("MASTER_POS_WAIT", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new MasterPosWait(arguments);
    }

}
