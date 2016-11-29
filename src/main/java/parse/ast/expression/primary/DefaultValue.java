/**
 * (created at 2011-1-27)
 */
package parse.ast.expression.primary;

import parse.visitor.SQLASTVisitor;

/**
 * used as right oprand for assignment of INSERT and REPLACE
 */
public class DefaultValue extends PrimaryExpression {

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
