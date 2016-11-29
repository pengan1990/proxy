package parse.ast.stmt.dml;

import parse.ast.expression.primary.Identifier;
import parse.visitor.SQLASTVisitor;

/**
 * Created by pengan on 16-11-4.
 */
public class DMLUseStatement extends DMLStatement {
    private Identifier schema;

    public DMLUseStatement(Identifier schema) {
        this.schema = schema;
        this.stmtType = StmtType.DML_USE;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        // do nothing
    }

    public Identifier getSchema() {
        return schema;
    }
}
