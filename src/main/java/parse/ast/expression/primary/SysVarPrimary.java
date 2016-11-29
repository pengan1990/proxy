/**
 * (created at 2011-1-21)
 */
package parse.ast.expression.primary;

import parse.ast.fragment.VariableScope;
import parse.visitor.SQLASTVisitor;

/**

 */
public class SysVarPrimary extends VariableExpression {
    private final VariableScope scope;
    /**
     * excluding starting "@@", '`' might be included
     */
    private final String varText;
    private final String varTextUp;

    public SysVarPrimary(VariableScope scope, String varText, String varTextUp) {
        this.scope = scope;
        this.varText = varText;
        this.varTextUp = varTextUp;
    }

    /**
     * @return never null
     */
    public VariableScope getScope() {
        return scope;
    }

    public String getVarTextUp() {
        return varTextUp;
    }

    public String getVarText() {
        return varText;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
