/**
 * (created at 2011-7-5)
 */
package parse.ast.stmt.ddl;

import parse.ast.expression.primary.Identifier;
import parse.util.Pair;
import parse.visitor.SQLASTVisitor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**

 */
public class DDLRenameTableStatement extends DDLStatement {
    private final List<Pair<Identifier, Identifier>> list;

    public DDLRenameTableStatement() {
        this.list = new LinkedList<Pair<Identifier, Identifier>>();
    }

    public DDLRenameTableStatement(List<Pair<Identifier, Identifier>> list) {
        if (list == null) {
            this.list = Collections.emptyList();
        } else {
            this.list = list;
        }
    }

    public DDLRenameTableStatement addRenamePair(Identifier from, Identifier to) {
        list.add(new Pair<Identifier, Identifier>(from, to));
        return this;
    }

    public List<Pair<Identifier, Identifier>> getList() {
        return list;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }

}
