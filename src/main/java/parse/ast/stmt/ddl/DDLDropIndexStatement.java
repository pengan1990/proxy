/**
 * (created at 2011-7-5)
 */
package parse.ast.stmt.ddl;

import parse.ast.expression.primary.Identifier;
import parse.visitor.SQLASTVisitor;

/**

 */
public class DDLDropIndexStatement extends DDLStatement {
    private final Identifier indexName;
    private final Identifier table;

    public DDLDropIndexStatement(Identifier indexName, Identifier table) {
        this.indexName = indexName;
        this.table = table;
    }

    public Identifier getIndexName() {
        return indexName;
    }

    public Identifier getTable() {
        return table;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
