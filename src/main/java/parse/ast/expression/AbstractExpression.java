/**
 * (created at 2011-7-25)
 */
package parse.ast.expression;

import java.util.Map;

/**

 */
public abstract class AbstractExpression implements Expression {
    private boolean cacheEvalRst = true;
    private boolean evaluated;
    private Object evaluationCache;

    @Override
    public Expression setCacheEvalRst(boolean cacheEvalRst) {
        this.cacheEvalRst = cacheEvalRst;
        return this;
    }

    @Override
    public final Object evaluation(Map<? extends Object, ? extends Object> parameters) {
        if (cacheEvalRst) {
            if (evaluated) {
                return evaluationCache;
            }
            evaluationCache = evaluationInternal(parameters);
            evaluated = true;
            return evaluationCache;
        }
        return evaluationInternal(parameters);
    }

    protected abstract Object evaluationInternal(Map<? extends Object, ? extends Object> parameters);

}
