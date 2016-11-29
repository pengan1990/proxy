/**
 * (created at 2011-1-23)
 */
package parse.ast;

import parse.visitor.SQLASTVisitor;

/**

 */
public interface ASTNode {
    void accept(SQLASTVisitor visitor);
}
