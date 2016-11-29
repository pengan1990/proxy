/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary.function.info;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.function.FunctionExpression;

import java.util.List;

/**

 */
public class Benchmark extends FunctionExpression {
    public Benchmark(List<Expression> arguments) {
        super("BENCHMARK", arguments);
    }

    @Override
    public FunctionExpression constructFunction(List<Expression> arguments) {
        return new Benchmark(arguments);
    }

}
