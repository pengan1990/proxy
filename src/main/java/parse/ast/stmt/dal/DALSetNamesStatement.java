/**
 * (created at 2011-9-23)
 */
package parse.ast.stmt.dal;

import parse.ast.stmt.SQLStatement;
import parse.visitor.SQLASTVisitor;

/**

 */
public class DALSetNamesStatement implements SQLStatement {
    private final String charsetName;
    private final String collationName;


    protected StmtType stmtType = StmtType.DAL_SET;

    public DALSetNamesStatement() {
        this.charsetName = null;
        this.collationName = null;
    }

    public DALSetNamesStatement(String charsetName, String collationName) {
        if (charsetName == null) throw new IllegalArgumentException("charsetName is null");
        this.charsetName = charsetName;
        this.collationName = collationName;
    }

    @Override
    public StmtType getStmtType() {
        return stmtType;
    }

    public boolean isDefault() {
        return charsetName == null;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public String getCollationName() {
        return collationName;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
