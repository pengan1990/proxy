/**
 * (created at 2011-1-19)
 */
package parse.ast.expression.primary;

import parse.ast.expression.Expression;
import parse.util.Pair;
import parse.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>'CASE' value? ('WHEN' condition 'THEN' result)+ ('ELSE' result)? 'END' </code>
 */
public class CaseWhenOperatorExpression extends PrimaryExpression {
    private final Expression comparee;
    private final List<Pair<Expression, Expression>> whenList;
    private final Expression elseResult;

    /**
     * @param whenList never null or empry; no pair contains null key or value
     * @param comparee null for format of <code>CASE WHEN ...</code>, otherwise,
     *                 <code>CASE comparee WHEN ...</code>
     */
    public CaseWhenOperatorExpression(Expression comparee, List<Pair<Expression, Expression>> whenList,
                                      Expression elseResult) {
        super();
        this.comparee = comparee;
        if (whenList instanceof ArrayList) {
            this.whenList = whenList;
        } else {
            this.whenList = new ArrayList<Pair<Expression, Expression>>(whenList);
        }
        this.elseResult = elseResult;
    }

    public Expression getComparee() {
        return comparee;
    }

    /**
     * @return never null or empty; no pair contains null key or value
     */
    public List<Pair<Expression, Expression>> getWhenList() {
        return whenList;
    }

    public Expression getElseResult() {
        return elseResult;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
