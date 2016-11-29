/**
 * (created at 2011-1-26)
 */
package parse.ast.expression.misc;

import parse.ast.expression.AbstractExpression;
import parse.ast.expression.Expression;
import parse.visitor.SQLASTVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**

 */
public class InExpressionList extends AbstractExpression {
    private List<Expression> list;
    private List<Expression> replaceList;

    public InExpressionList(List<Expression> list) {
        if (list == null || list.size() == 0) {
            this.list = Collections.emptyList();
        } else if (list instanceof ArrayList) {
            this.list = list;
        } else {
            this.list = new ArrayList<Expression>(list);
        }
    }

    /**
     * @return never null
     */
    public List<Expression> getList() {
        return list;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_PRIMARY;
    }

    @Override
    public Object evaluationInternal(Map<? extends Object, ? extends Object> parameters) {
        return UNEVALUATABLE;
    }

    public void setReplaceExpr(List<Expression> replaceList) {
        this.replaceList = replaceList;
    }

    public void clearReplaceExpr() {
        this.replaceList = null;
    }

    @Override
    public void accept(SQLASTVisitor visitor) {
        if (replaceList == null) {
            visitor.visit(this);
        } else {
            List<Expression> temp = list;
            list = replaceList;
            visitor.visit(this);
            list = temp;
        }
    }
}
