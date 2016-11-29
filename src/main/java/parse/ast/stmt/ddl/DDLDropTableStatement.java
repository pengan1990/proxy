/**
 * (created at 2011-7-5)
 */
package parse.ast.stmt.ddl;

import parse.ast.expression.primary.Identifier;
import parse.visitor.SQLASTVisitor;

import java.util.Collections;
import java.util.List;

/**

 */
public class DDLDropTableStatement extends DDLStatement {
    private final List<Identifier> tableNames;
    private final boolean temp;
    private final boolean ifExists;
    private final Mode mode;
    public DDLDropTableStatement(List<Identifier> tableNames, boolean temp, boolean ifExists) {
        this(tableNames, temp, ifExists, Mode.UNDEF);
    }

    public DDLDropTableStatement(List<Identifier> tableNames, boolean temp, boolean ifExists, Mode mode) {
        if (tableNames == null || tableNames.isEmpty()) {
            this.tableNames = Collections.emptyList();
        } else {
            this.tableNames = tableNames;
        }
        this.temp = temp;
        this.ifExists = ifExists;
        this.mode = mode;
    }

    public List<Identifier> getTableNames() {
        return tableNames;
    }

    public boolean isTemp() {
        return temp;
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public static enum Mode {
        UNDEF,
        RESTRICT,
        CASCADE
    }

}
