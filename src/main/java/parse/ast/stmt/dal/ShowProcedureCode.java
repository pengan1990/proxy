/**
 * (created at 2011-5-20)
 */
package parse.ast.stmt.dal;

import parse.ast.expression.primary.Identifier;
import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowProcedureCode extends DALShowStatement {
    private final Identifier procedureName;

    public ShowProcedureCode(Identifier procedureName) {
        this.procedureName = procedureName;
    }

    public Identifier getProcedureName() {
        return procedureName;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
