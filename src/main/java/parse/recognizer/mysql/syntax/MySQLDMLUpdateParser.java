/**
 * (created at 2011-5-19)
 */
package parse.recognizer.mysql.syntax;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.Identifier;
import parse.ast.fragment.Limit;
import parse.ast.fragment.OrderBy;
import parse.ast.fragment.tableref.TableReferences;
import parse.ast.stmt.dml.DMLUpdateStatement;
import parse.recognizer.mysql.lexer.MySQLLexer;
import parse.util.Pair;
import parse.recognizer.mysql.MySQLToken;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**

 */
public class MySQLDMLUpdateParser extends MySQLDMLParser {
    public MySQLDMLUpdateParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer, exprParser);
    }

    /**
     * nothing has been pre-consumed <code><pre>
     * 'UPDATE' 'LOW_PRIORITY'? 'IGNORE'? table_reference
     *   'SET' colName ('='|':=') (expr|'DEFAULT') (',' colName ('='|':=') (expr|'DEFAULT'))*
     *     ('WHERE' cond)?
     *     {singleTable}? => ('ORDER' 'BY' orderBy)?  ('LIMIT' count)?
     * </pre></code>
     */
    public DMLUpdateStatement update() throws SQLSyntaxErrorException {
        match(MySQLToken.KW_UPDATE);
        boolean lowPriority = false;
        boolean ignore = false;
        if (lexer.token() == MySQLToken.KW_LOW_PRIORITY) {
            lexer.nextToken();
            lowPriority = true;
        }
        if (lexer.token() == MySQLToken.KW_IGNORE) {
            lexer.nextToken();
            ignore = true;
        }
        TableReferences tableRefs = tableRefs();
        match(MySQLToken.KW_SET);
        List<Pair<Identifier, Expression>> values;
        Identifier col = identifier();
        match(MySQLToken.OP_EQUALS, MySQLToken.OP_ASSIGN);
        Expression expr = exprParser.expression();
        if (lexer.token() == MySQLToken.PUNC_COMMA) {
            values = new LinkedList<Pair<Identifier, Expression>>();
            values.add(new Pair<Identifier, Expression>(col, expr));
            for (; lexer.token() == MySQLToken.PUNC_COMMA; ) {
                lexer.nextToken();
                col = identifier();
                match(MySQLToken.OP_EQUALS, MySQLToken.OP_ASSIGN);
                expr = exprParser.expression();
                values.add(new Pair<Identifier, Expression>(col, expr));
            }
        } else {
            values = new ArrayList<Pair<Identifier, Expression>>(1);
            values.add(new Pair<Identifier, Expression>(col, expr));
        }
        Expression where = null;
        if (lexer.token() == MySQLToken.KW_WHERE) {
            lexer.nextToken();
            where = exprParser.expression();
        }
        OrderBy orderBy = null;
        Limit limit = null;
        if (tableRefs.isSingleTable()) {
            orderBy = orderBy();
            limit = limit();
        }
        return new DMLUpdateStatement(lowPriority, ignore, tableRefs, values, where, orderBy, limit);
    }
}
