/**
 * (created at 2011-4-13)
 */
package parse.ast.expression.misc;

import parse.ast.expression.BinaryOperatorExpression;
import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

/**

 */
public class AssignmentExpression extends BinaryOperatorExpression {
    public AssignmentExpression(Expression left, Expression right) {
        super(left, right, Expression.PRECEDENCE_ASSIGNMENT, false);
    }

    public String getOperator() {
        return ":=";
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
