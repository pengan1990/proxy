/**
 * (created at 2011-5-21)
 */
package parse.ast.stmt.dal;

import parse.ast.expression.Expression;
import parse.ast.fragment.Limit;
import parse.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**

 */
public class ShowProfile extends DALShowStatement {
    private final List<Type> types;
    private final Expression forQuery;
    private final Limit limit;
    public ShowProfile(List<Type> types, Expression forQuery, Limit limit) {
        if (types == null || types.isEmpty()) {
            this.types = Collections.emptyList();
        } else if (types instanceof ArrayList) {
            this.types = types;
        } else {
            this.types = new ArrayList<Type>(types);
        }
        this.forQuery = forQuery;
        this.limit = limit;
    }

    /**
     * @return never null
     */
    public List<Type> getTypes() {
        return types;
    }

    public Expression getForQuery() {
        return forQuery;
    }

    public Limit getLimit() {
        return limit;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * enum name must equals to real sql while ' ' is replaced with '_'
     */
    public static enum Type {
        ALL,
        BLOCK_IO,
        CONTEXT_SWITCHES,
        CPU,
        IPC,
        MEMORY,
        PAGE_FAULTS,
        SOURCE,
        SWAPS
    }
}
