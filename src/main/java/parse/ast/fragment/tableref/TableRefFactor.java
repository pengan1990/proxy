/**
 * (created at 2011-2-9)
 */
package parse.ast.fragment.tableref;

import parse.ast.expression.primary.Identifier;
import parse.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**

 */
public class TableRefFactor extends AliasableTableReference {
    /**
     * e.g. <code>"`db2`.`tb1`"</code> is possible
     */
    private final Identifier table;
    private final List<IndexHint> hintList;

    public TableRefFactor(Identifier table, String alias, List<IndexHint> hintList) {
        super(alias);
        this.table = table;
        if (hintList == null || hintList.isEmpty()) {
            this.hintList = Collections.emptyList();
        } else if (hintList instanceof ArrayList) {
            this.hintList = hintList;
        } else {
            this.hintList = new ArrayList<IndexHint>(hintList);
        }
    }

    public TableRefFactor(Identifier table, List<IndexHint> hintList) {
        this(table, null, hintList);
    }

    public Identifier getTable() {
        return table;
    }

    public List<IndexHint> getHintList() {
        return hintList;
    }

    @Override
    public Object removeLastConditionElement() {
        return null;
    }

    @Override
    public boolean isSingleTable() {
        return true;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_FACTOR;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
