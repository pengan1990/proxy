package parse.ast.stmt.dml;

import parse.ast.expression.Expression;
import parse.ast.expression.misc.QueryExpression;
import parse.ast.fragment.Limit;

import java.util.Map;

public abstract class DMLQueryStatement extends DMLStatement implements QueryExpression {

    @Override
    public int getPrecedence() {
        return PRECEDENCE_QUERY;
    }

    @Override
    public Expression setCacheEvalRst(boolean cacheEvalRst) {
        return this;
    }

    @Override
    public Object evaluation(Map<? extends Object, ? extends Object> parameters) {
        return UNEVALUATABLE;
    }

    public abstract Limit getLimit();

    public abstract void setLimit(Limit limit);
}
