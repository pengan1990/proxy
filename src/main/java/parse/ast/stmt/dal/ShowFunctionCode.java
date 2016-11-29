/**
 * (created at 2011-5-21)
 */
package parse.ast.stmt.dal;

import parse.ast.expression.primary.Identifier;
import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowFunctionCode extends DALShowStatement {
    private final Identifier functionName;

    public ShowFunctionCode(Identifier functionName) {
        this.functionName = functionName;
    }

    public Identifier getFunctionName() {
        return functionName;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
