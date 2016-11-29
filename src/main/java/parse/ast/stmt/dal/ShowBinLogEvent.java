/**
 * (created at 2011-5-20)
 */
package parse.ast.stmt.dal;

import parse.ast.expression.Expression;
import parse.ast.fragment.Limit;
import parse.visitor.SQLASTVisitor;

/**

 */
public class ShowBinLogEvent extends DALShowStatement {
    private final String logName;
    private final Expression pos;
    private final Limit limit;

    public ShowBinLogEvent(String logName, Expression pos, Limit limit) {
        this.logName = logName;
        this.pos = pos;
        this.limit = limit;
    }

    public String getLogName() {
        return logName;
    }

    public Expression getPos() {
        return pos;
    }

    public Limit getLimit() {
        return limit;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
