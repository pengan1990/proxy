/**
 * (created at 2011-5-19)
 */
package parse.recognizer.mysql.syntax;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.RowExpression;
import parse.recognizer.mysql.MySQLToken;
import parse.recognizer.mysql.lexer.MySQLLexer;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**

 */
public abstract class MySQLDMLInsertReplaceParser extends MySQLDMLParser {
    public MySQLDMLInsertReplaceParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer, exprParser);
    }

    protected List<RowExpression> rowList() throws SQLSyntaxErrorException {
        List<RowExpression> valuesList;
        List<Expression> tempRowValue = rowValue();
        if (lexer.token() == MySQLToken.PUNC_COMMA) {
            valuesList = new LinkedList<RowExpression>();
            valuesList.add(new RowExpression(tempRowValue));
            for (; lexer.token() == MySQLToken.PUNC_COMMA; ) {
                lexer.nextToken();
                tempRowValue = rowValue();
                valuesList.add(new RowExpression(tempRowValue));
            }
        } else {
            valuesList = new ArrayList<RowExpression>(1);
            valuesList.add(new RowExpression(tempRowValue));
        }
        return valuesList;
    }

    /**
     * first token is <code>(</code>
     */
    private List<Expression> rowValue() throws SQLSyntaxErrorException {
        match(MySQLToken.PUNC_LEFT_PAREN);
        if (lexer.token() == MySQLToken.PUNC_RIGHT_PAREN) {
            return Collections.emptyList();
        }
        List<Expression> row;
        Expression expr = exprParser.expression();
        if (lexer.token() == MySQLToken.PUNC_COMMA) {
            row = new LinkedList<Expression>();
            row.add(expr);
            for (; lexer.token() == MySQLToken.PUNC_COMMA; ) {
                lexer.nextToken();
                expr = exprParser.expression();
                row.add(expr);
            }
        } else {
            row = new ArrayList<Expression>(1);
            row.add(expr);
        }
        match(MySQLToken.PUNC_RIGHT_PAREN);
        return row;
    }
}
