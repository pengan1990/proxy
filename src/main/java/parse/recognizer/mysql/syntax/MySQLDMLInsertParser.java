/**
 * (created at 2011-5-18)
 */
package parse.recognizer.mysql.syntax;

import parse.ast.expression.Expression;
import parse.ast.expression.misc.QueryExpression;
import parse.ast.expression.primary.Identifier;
import parse.ast.expression.primary.RowExpression;
import parse.ast.stmt.dml.DMLInsertStatement;
import parse.recognizer.mysql.lexer.MySQLLexer;
import parse.util.Pair;
import parse.recognizer.mysql.MySQLToken;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**

 */
public class MySQLDMLInsertParser extends MySQLDMLInsertReplaceParser {
    public MySQLDMLInsertParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer, exprParser);
    }

    /**
     * nothing has been pre-consumed <code><pre>
     * 'INSERT' ('LOW_PRIORITY'|'DELAYED'|'HIGH_PRIORITY')? 'IGNORE'? 'INTO'? tbname
     *  (  'SET' colName ('='|':=') (expr|'DEFAULT') (',' colName ('='|':=') (expr|'DEFAULT'))*
     *   | '(' (  colName (',' colName)* ')' ( ('VALUES'|'VALUE') value (',' value)*
     *                                        | '(' 'SELECT' ... ')'
     *                                        | 'SELECT' ...
     *                                       )
     *          | 'SELECT' ... ')'
     *         )
     *   |('VALUES'|'VALUE') value  ( ',' value )*
     *   | 'SELECT' ...
     *  )
     * ( 'ON' 'DUPLICATE' 'KEY' 'UPDATE' colName ('='|':=') expr ( ',' colName ('='|':=') expr)* )?
     * <p/>
     * value := '(' (expr|'DEFAULT') ( ',' (expr|'DEFAULT'))* ')'
     * </pre></code>
     */
    public DMLInsertStatement insert() throws SQLSyntaxErrorException {
        match(MySQLToken.KW_INSERT);
        DMLInsertStatement.InsertMode mode = DMLInsertStatement.InsertMode.UNDEF;
        boolean ignore = false;
        switch (lexer.token()) {
            case KW_LOW_PRIORITY:
                lexer.nextToken();
                mode = DMLInsertStatement.InsertMode.LOW;
                break;
            case KW_DELAYED:
                lexer.nextToken();
                mode = DMLInsertStatement.InsertMode.DELAY;
                break;
            case KW_HIGH_PRIORITY:
                lexer.nextToken();
                mode = DMLInsertStatement.InsertMode.HIGH;
                break;
        }
        if (lexer.token() == MySQLToken.KW_IGNORE) {
            ignore = true;
            lexer.nextToken();
        }
        if (lexer.token() == MySQLToken.KW_INTO) {
            lexer.nextToken();
        }
        Identifier table = identifier();
        List<Pair<Identifier, Expression>> dupUpdate;
        List<Identifier> columnNameList;
        List<RowExpression> rowList;
        QueryExpression select;

        List<Expression> tempRowValue;
        switch (lexer.token()) {
            case KW_SET:
                lexer.nextToken();
                columnNameList = new LinkedList<Identifier>();
                tempRowValue = new LinkedList<Expression>();
                for (; ; lexer.nextToken()) {
                    Identifier id = identifier();
                    match(MySQLToken.OP_EQUALS, MySQLToken.OP_ASSIGN);
                    Expression expr = exprParser.expression();
                    columnNameList.add(id);
                    tempRowValue.add(expr);
                    if (lexer.token() != MySQLToken.PUNC_COMMA) {
                        break;
                    }
                }
                rowList = new ArrayList<RowExpression>(1);
                rowList.add(new RowExpression(tempRowValue));
                dupUpdate = onDuplicateUpdate();
                return new DMLInsertStatement(mode, ignore, table, columnNameList, rowList, dupUpdate);
            case IDENTIFIER:
                if (!"VALUE".equals(lexer.stringValueUppercase())) {
                    break;
                }
            case KW_VALUES:
                lexer.nextToken();
                columnNameList = null;
                rowList = rowList();
                dupUpdate = onDuplicateUpdate();
                return new DMLInsertStatement(mode, ignore, table, columnNameList, rowList, dupUpdate);
            case KW_SELECT:
                columnNameList = null;
                select = select();
                dupUpdate = onDuplicateUpdate();
                return new DMLInsertStatement(mode, ignore, table, columnNameList, select, dupUpdate);
            case PUNC_LEFT_PAREN:
                switch (lexer.nextToken()) {
                    case PUNC_LEFT_PAREN:
                    case KW_SELECT:
                        columnNameList = null;
                        select = selectPrimary();
                        match(MySQLToken.PUNC_RIGHT_PAREN);
                        dupUpdate = onDuplicateUpdate();
                        return new DMLInsertStatement(mode, ignore, table, columnNameList, select, dupUpdate);
                }
                columnNameList = idList();
                match(MySQLToken.PUNC_RIGHT_PAREN);
                switch (lexer.token()) {
                    case PUNC_LEFT_PAREN:
                    case KW_SELECT:
                        select = selectPrimary();
                        dupUpdate = onDuplicateUpdate();
                        return new DMLInsertStatement(mode, ignore, table, columnNameList, select, dupUpdate);
                    case KW_VALUES:
                        lexer.nextToken();
                        break;
                    default:
                        matchIdentifier("VALUE");
                }
                rowList = rowList();
                dupUpdate = onDuplicateUpdate();
                return new DMLInsertStatement(mode, ignore, table, columnNameList, rowList, dupUpdate);
        }
        throw err("unexpected token for insert: " + lexer.token());
    }

    /**
     * @return null for not exist
     */
    private List<Pair<Identifier, Expression>> onDuplicateUpdate() throws SQLSyntaxErrorException {
        if (lexer.token() != MySQLToken.KW_ON) {
            return null;
        }
        lexer.nextToken();
        matchIdentifier("DUPLICATE");
        match(MySQLToken.KW_KEY);
        match(MySQLToken.KW_UPDATE);
        List<Pair<Identifier, Expression>> list;
        Identifier col = identifier();
        match(MySQLToken.OP_EQUALS, MySQLToken.OP_ASSIGN);
        Expression expr = exprParser.expression();
        if (lexer.token() == MySQLToken.PUNC_COMMA) {
            list = new LinkedList<Pair<Identifier, Expression>>();
            list.add(new Pair<Identifier, Expression>(col, expr));
            for (; lexer.token() == MySQLToken.PUNC_COMMA; ) {
                lexer.nextToken();
                col = identifier();
                match(MySQLToken.OP_EQUALS, MySQLToken.OP_ASSIGN);
                expr = exprParser.expression();
                list.add(new Pair<Identifier, Expression>(col, expr));
            }
            return list;
        }
        list = new ArrayList<Pair<Identifier, Expression>>(1);
        list.add(new Pair<Identifier, Expression>(col, expr));
        return list;
    }
}
