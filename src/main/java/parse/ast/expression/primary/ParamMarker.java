/**
 * (created at 2011-1-21)
 */
package parse.ast.expression.primary;

import parse.visitor.SQLASTVisitor;

import java.util.Map;

/**
 * <code>'?'</code>
 */
public class ParamMarker extends PrimaryExpression {
    private final int paramIndex;

    /**
     * @param paramIndex start from 1
     */
    public ParamMarker(int paramIndex) {
        this.paramIndex = paramIndex;
    }

    /**
     * @return start from 1
     */
    public int getParamIndex() {
        return paramIndex;
    }

    @Override
    public int hashCode() {
        return paramIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ParamMarker) {
            ParamMarker that = (ParamMarker) obj;
            return this.paramIndex == that.paramIndex;
        }
        return false;
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return parameters.get(paramIndex);
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
