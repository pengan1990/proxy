/**
 * (created at 2011-5-19)
 */
package parse.ast.stmt.dml;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.Identifier;
import parse.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**

 */
public class DMLCallStatement extends DMLStatement {
    private final Identifier procedure;
    private final List<Expression> arguments;

    public DMLCallStatement(Identifier procedure, List<Expression> arguments) {
        this.procedure = procedure;
        if (arguments == null || arguments.isEmpty()) {
            this.arguments = Collections.emptyList();
        } else if (arguments instanceof ArrayList) {
            this.arguments = arguments;
        } else {
            this.arguments = new ArrayList<Expression>(arguments);
        }

        this.stmtType = StmtType.DML_CALL;//by zcy
    }

    public DMLCallStatement(Identifier procedure) {
        this.procedure = procedure;
        this.arguments = Collections.emptyList();

        this.stmtType = StmtType.DML_CALL;//by zcy
    }

    public Identifier getProcedure() {
        return procedure;
    }

    /**
     * @return never null
     */
    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        visitor.visit(this);
    }
}
