/**
 * (created at 2011-6-3)
 */
package parse.ast.expression.misc;

import parse.ast.expression.primary.PrimaryExpression;
import parse.visitor.SQLASTVisitor;

/**

 */
public class UserExpression extends PrimaryExpression {
    private final String userAtHost;

    /**
     * @param userAtHost
     */
    public UserExpression(String userAtHost) {
        super();
        this.userAtHost = userAtHost;
    }

    public String getUserAtHost() {
        return userAtHost;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
