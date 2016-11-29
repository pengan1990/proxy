/**
 * (created at 2011-5-19)
 */
package parse.ast.stmt.dal;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.VariableExpression;
import parse.ast.stmt.SQLStatement;
import parse.util.Pair;
import parse.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**

 */
public class DALSetStatement implements SQLStatement {
    private final List<Pair<VariableExpression, Expression>> assignmentList;

    private StmtType stmtType = StmtType.DAL_SET;

    public DALSetStatement(List<Pair<VariableExpression, Expression>> assignmentList) {
        if (assignmentList == null || assignmentList.isEmpty()) {
            this.assignmentList = Collections.emptyList();
        } else if (assignmentList instanceof ArrayList) {
            this.assignmentList = assignmentList;
        } else {
            this.assignmentList = new ArrayList<Pair<VariableExpression, Expression>>(assignmentList);
        }
    }

    @Override
    public StmtType getStmtType() {
        return stmtType;
    }

    /**
     * @return never null
     */
    public List<Pair<VariableExpression, Expression>> getAssignmentList() {
        return assignmentList;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
