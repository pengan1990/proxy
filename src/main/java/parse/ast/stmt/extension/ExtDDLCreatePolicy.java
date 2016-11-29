/**
 * (created at 2012-8-14)
 */
package parse.ast.stmt.extension;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.Identifier;
import parse.ast.stmt.ddl.DDLStatement;
import parse.util.Pair;
import parse.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.List;

/**

 */
public class ExtDDLCreatePolicy extends DDLStatement {
    private final Identifier name;
    private final List<Pair<Integer, Expression>> proportion;

    public ExtDDLCreatePolicy(Identifier name) {
        this.name = name;
        this.proportion = new ArrayList<Pair<Integer, Expression>>(1);
    }

    public Identifier getName() {
        return name;
    }

    public List<Pair<Integer, Expression>> getProportion() {
        return proportion;
    }

    public ExtDDLCreatePolicy addProportion(Integer id, Expression val) {
        proportion.add(new Pair<Integer, Expression>(id, val));
        return this;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
