/**
 * (created at 2011-5-17)
 */
package parse.recognizer.mysql.syntax;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.Identifier;
import parse.ast.fragment.Limit;
import parse.ast.fragment.OrderBy;
import parse.ast.fragment.tableref.TableReferences;
import parse.ast.stmt.dml.DMLDeleteStatement;
import parse.recognizer.mysql.MySQLToken;
import parse.recognizer.mysql.lexer.MySQLLexer;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**

 */
public class MySQLDMLDeleteParser extends MySQLDMLParser {
    private static final Map<String, SpecialIdentifier> specialIdentifiers = new HashMap<String, SpecialIdentifier>();

    static {
        specialIdentifiers.put("QUICK", SpecialIdentifier.QUICK);
    }

    public MySQLDMLDeleteParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer, exprParser);
    }

    /**
     * first token is {@link MySQLToken#KW_DELETE} <code><pre>
     * 'DELETE' 'LOW_PRIORITY'? 'QUICK'? 'IGNORE'? (
     *     'FROM' tid ( (',' tid)* 'USING' table_refs ('WHERE' cond)?
     *                | ('WHERE' cond)? ('ORDER' 'BY' ids)? ('LIMIT' count)?  )  // single table
     *    | tid (',' tid)* 'FROM' table_refs ('WHERE' cond)? )
     * </pre></code>
     */
    public DMLDeleteStatement delete() throws SQLSyntaxErrorException {
        match(MySQLToken.KW_DELETE);
        boolean lowPriority = false;
        boolean quick = false;
        boolean ignore = false;
        loopOpt:
        for (; ; lexer.nextToken()) {
            switch (lexer.token()) {
                case KW_LOW_PRIORITY:
                    lowPriority = true;
                    break;
                case KW_IGNORE:
                    ignore = true;
                    break;
                case IDENTIFIER:
                    SpecialIdentifier si = specialIdentifiers.get(lexer.stringValueUppercase());
                    if (SpecialIdentifier.QUICK == si) {
                        quick = true;
                        break;
                    }
                default:
                    break loopOpt;
            }
        }
        List<Identifier> tempList;
        TableReferences tempRefs;
        Expression tempWhere;
        if (lexer.token() == MySQLToken.KW_FROM) {
            lexer.nextToken();
            Identifier id = identifier();
            tempList = new ArrayList<Identifier>(1);
            tempList.add(id);
            switch (lexer.token()) {
                case PUNC_COMMA:
                    tempList = buildIdList(id);
                case KW_USING:
                    lexer.nextToken();
                    tempRefs = tableRefs();
                    if (lexer.token() == MySQLToken.KW_WHERE) {
                        lexer.nextToken();
                        tempWhere = exprParser.expression();
                        return new DMLDeleteStatement(lowPriority, quick, ignore, tempList, tempRefs, tempWhere);
                    }
                    return new DMLDeleteStatement(lowPriority, quick, ignore, tempList, tempRefs);
                case KW_WHERE:
                case KW_ORDER:
                case KW_LIMIT:
                    break;
                default:
                    return new DMLDeleteStatement(lowPriority, quick, ignore, id);
            }
            tempWhere = null;
            OrderBy orderBy = null;
            Limit limit = null;
            if (lexer.token() == MySQLToken.KW_WHERE) {
                lexer.nextToken();
                tempWhere = exprParser.expression();
            }
            if (lexer.token() == MySQLToken.KW_ORDER) {
                orderBy = orderBy();
            }
            if (lexer.token() == MySQLToken.KW_LIMIT) {
                limit = limit();
            }
            return new DMLDeleteStatement(lowPriority, quick, ignore, id, tempWhere, orderBy, limit);
        }

        tempList = idList();
        match(MySQLToken.KW_FROM);
        tempRefs = tableRefs();
        if (lexer.token() == MySQLToken.KW_WHERE) {
            lexer.nextToken();
            tempWhere = exprParser.expression();
            return new DMLDeleteStatement(lowPriority, quick, ignore, tempList, tempRefs, tempWhere);
        }
        return new DMLDeleteStatement(lowPriority, quick, ignore, tempList, tempRefs);
    }

    private static enum SpecialIdentifier {
        QUICK
    }

}
