/**
 * (created at 2011-1-21)
 */
package parse.ast.expression.primary;

import parse.visitor.SQLASTVisitor;

/**

 */
public class UsrDefVarPrimary extends VariableExpression {
    /**
     * include starting '@', e.g. "@'mary''s'"
     */
    private final String varText;

    public UsrDefVarPrimary(String varText) {
        this.varText = varText;
    }

    public String getVarText() {
        return varText;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
