/**
 * (created at 2011-1-23)
 */
package parse.ast.expression.primary;

import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**

 */
public class RowExpression extends PrimaryExpression {
    private final List<Expression> rowExprList;

    public RowExpression(List<Expression> rowExprList) {
        if (rowExprList == null || rowExprList.isEmpty()) {
            this.rowExprList = Collections.emptyList();
        } else if (rowExprList instanceof ArrayList) {
            this.rowExprList = rowExprList;
        } else {
            this.rowExprList = new ArrayList<Expression>(rowExprList);
        }
    }

    /**
     * @return never null
     */
    public List<Expression> getRowExprList() {
        return rowExprList;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
