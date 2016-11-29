/**
 * (created at 2011-5-19)
 */
package parse.ast.stmt.dml;

import parse.ast.expression.misc.QueryExpression;
import parse.ast.expression.primary.Identifier;
import parse.ast.expression.primary.RowExpression;
import parse.visitor.SQLASTVisitor;

import java.util.List;

/**

 */
public class DMLReplaceStatement extends DMLInsertReplaceStatement {
    private final ReplaceMode mode;

    public DMLReplaceStatement(ReplaceMode mode, Identifier table, List<Identifier> columnNameList,
                               List<RowExpression> rowList) {
        super(table, columnNameList, rowList);
        this.mode = mode;
    }

    public DMLReplaceStatement(ReplaceMode mode, Identifier table, List<Identifier> columnNameList,
                               QueryExpression select) {
        super(table, columnNameList, select);
        this.mode = mode;
    }

    public ReplaceMode getMode() {
        return mode;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    public static enum ReplaceMode {
        /**
         * default
         */
        UNDEF,
        LOW,
        DELAY
    }
}
