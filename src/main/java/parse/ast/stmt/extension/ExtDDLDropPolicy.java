/**
 * (created at 2012-8-14)
 */
package parse.ast.stmt.extension;

import parse.ast.expression.primary.Identifier;
import parse.ast.stmt.ddl.DDLStatement;
import parse.visitor.SQLASTVisitor;

/**

 */
public class ExtDDLDropPolicy extends DDLStatement {
    private final Identifier policyName;

    public ExtDDLDropPolicy(Identifier policyName) {
        this.policyName = policyName;
    }

    public Identifier getPolicyName() {
        return policyName;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
