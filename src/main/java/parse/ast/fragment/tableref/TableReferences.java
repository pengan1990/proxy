/**
 * (created at 2011-1-25)
 */
package parse.ast.fragment.tableref;

import parse.visitor.SQLASTVisitor;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;

/**
 * used in <code>FROM</code> fragment
 */
public class TableReferences implements TableReference {
    private final List<TableReference> list;

    public TableReferences(List<TableReference> list) throws SQLSyntaxErrorException {
        if (list == null || list.isEmpty()) {
            throw new SQLSyntaxErrorException("at least one table reference");
        }
        this.list = ensureListType(list);
    }

    protected static List<TableReference> ensureListType(List<TableReference> list) {
        if (list instanceof ArrayList) return list;
        return new ArrayList<TableReference>(list);
    }

    /**
     * @return never null
     */
    public List<TableReference> getTableReferenceList() {
        return list;
    }

    @Override
    public Object removeLastConditionElement() {
        if (list != null && !list.isEmpty()) {
            return list.get(list.size() - 1).removeLastConditionElement();
        }
        return null;
    }

    @Override
    public boolean isSingleTable() {
        if (list == null) {
            return false;
        }
        int count = 0;
        TableReference first = null;
        for (TableReference ref : list) {
            if (ref != null && 1 == ++count) {
                first = ref;
            }
        }
        return count == 1 && first.isSingleTable();
    }

    @Override
    public int getPrecedence() {
        return TableReference.PRECEDENCE_REFS;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
