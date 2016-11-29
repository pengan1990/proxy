/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.encryption;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class AesDecrypt extends FunctionExpression {
    public AesDecrypt(List<Expression> arguments) {
        super("AES_DECRYPT", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new AesDecrypt(arguments);
    }

}
