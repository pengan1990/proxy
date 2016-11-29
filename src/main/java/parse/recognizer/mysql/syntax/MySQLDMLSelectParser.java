/**
 * (created at 2011-5-13)
 */
package parse.recognizer.mysql.syntax;

import parse.ast.expression.Expression;
import parse.ast.fragment.GroupBy;
import parse.ast.fragment.Limit;
import parse.ast.fragment.OrderBy;
import parse.ast.fragment.tableref.Dual;
import parse.ast.fragment.tableref.TableReference;
import parse.ast.fragment.tableref.TableReferences;
import parse.ast.stmt.dml.DMLQueryStatement;
import parse.ast.stmt.dml.DMLSelectStatement;
import parse.ast.stmt.dml.DMLSelectUnionStatement;
import parse.recognizer.mysql.MySQLToken;
import parse.recognizer.mysql.lexer.MySQLLexer;
import parse.util.Pair;

import java.sql.SQLSyntaxErrorException;
import java.util.*;

/**

 */
public class MySQLDMLSelectParser extends MySQLDMLParser {
    private static final Map<String, SpecialIdentifier> specialIdentifiers = new HashMap<String, SpecialIdentifier>();

    static {
        specialIdentifiers.put("SQL_BUFFER_RESULT", SpecialIdentifier.SQL_BUFFER_RESULT);
        specialIdentifiers.put("SQL_CACHE", SpecialIdentifier.SQL_CACHE);
        specialIdentifiers.put("SQL_NO_CACHE", SpecialIdentifier.SQL_NO_CACHE);
    }

    public MySQLDMLSelectParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer, exprParser);
        this.exprParser.setSelectParser(this);
    }

    private DMLSelectStatement.SelectOption selectOption() throws SQLSyntaxErrorException {
        for (DMLSelectStatement.SelectOption option = new DMLSelectStatement.SelectOption(); ; lexer.nextToken()) {
            outer:
            switch (lexer.token()) {
                case KW_ALL:
                    option.resultDup = DMLSelectStatement.SelectDuplicationStrategy.ALL;
                    break outer;
                case KW_DISTINCT:
                    option.resultDup = DMLSelectStatement.SelectDuplicationStrategy.DISTINCT;
                    break outer;
                case KW_DISTINCTROW:
                    option.resultDup = DMLSelectStatement.SelectDuplicationStrategy.DISTINCTROW;
                    break outer;
                case KW_HIGH_PRIORITY:
                    option.highPriority = true;
                    break outer;
                case KW_STRAIGHT_JOIN:
                    option.straightJoin = true;
                    break outer;
                case KW_SQL_SMALL_RESULT:
                    option.resultSize = DMLSelectStatement.SmallOrBigResult.SQL_SMALL_RESULT;
                    break outer;
                case KW_SQL_BIG_RESULT:
                    option.resultSize = DMLSelectStatement.SmallOrBigResult.SQL_BIG_RESULT;
                    break outer;
                case KW_SQL_CALC_FOUND_ROWS:
                    option.sqlCalcFoundRows = true;
                    break outer;
                case IDENTIFIER:
                    String optionStringUp = lexer.stringValueUppercase();
                    SpecialIdentifier specialId = specialIdentifiers.get(optionStringUp);
                    if (specialId != null) {
                        switch (specialId) {
                            case SQL_BUFFER_RESULT:
                                if (option.sqlBufferResult) return option;
                                option.sqlBufferResult = true;
                                break outer;
                            case SQL_CACHE:
                                if (option.queryCache != DMLSelectStatement.QueryCacheStrategy.UNDEF) return option;
                                option.queryCache = DMLSelectStatement.QueryCacheStrategy.SQL_CACHE;
                                break outer;
                            case SQL_NO_CACHE:
                                if (option.queryCache != DMLSelectStatement.QueryCacheStrategy.UNDEF) return option;
                                option.queryCache = DMLSelectStatement.QueryCacheStrategy.SQL_NO_CACHE;
                                break outer;
                        }
                    }
                default:
                    return option;
            }
        }
    }

    private List<Pair<Expression, String>> selectExprList() throws SQLSyntaxErrorException {
        Expression expr = exprParser.expression();
        String alias = as();
        List<Pair<Expression, String>> list;
        if (lexer.token() == MySQLToken.PUNC_COMMA) {
            list = new LinkedList<Pair<Expression, String>>();
            list.add(new Pair<Expression, String>(expr, alias));
        } else {
            list = new ArrayList<Pair<Expression, String>>(1);
            list.add(new Pair<Expression, String>(expr, alias));
            return list;
        }
        for (; lexer.token() == MySQLToken.PUNC_COMMA; list.add(new Pair<Expression, String>(expr, alias))) {
            lexer.nextToken();
            expr = exprParser.expression();
            alias = as();
        }
        return list;
    }

    @Override
    public DMLSelectStatement select() throws SQLSyntaxErrorException {//TODO 如果直接在解析的时候就把相关的字段处理好了，好像也是可以的
        match(MySQLToken.KW_SELECT);
        DMLSelectStatement.SelectOption option = selectOption();//解析Option选项
        List<Pair<Expression, String>> exprList = selectExprList();//解析select_expr, 对应的是expr
        TableReferences tables = null;
        Expression where = null;
        GroupBy group = null;
        Expression having = null;
        OrderBy order = null;
        Limit limit = null;

        boolean dual = false;
        if (lexer.token() == MySQLToken.KW_FROM) {
            if (lexer.nextToken() == MySQLToken.KW_DUAL) {
                lexer.nextToken();
                dual = true;
                List<TableReference> trs = new ArrayList<TableReference>(1);
                trs.add(new Dual());
                tables = new TableReferences(trs);
            } else {
                tables = tableRefs();
            }
        }
        if (lexer.token() == MySQLToken.KW_WHERE) {
            lexer.nextToken();
            where = exprParser.expression();
        }
        if (!dual) {
            group = groupBy();
            if (lexer.token() == MySQLToken.KW_HAVING) {
                lexer.nextToken();
                having = exprParser.expression();
            }
            order = orderBy();//TODO 需要将order中的表达式与select中的表达式对比一下
        }
        limit = limit();
        if (!dual) {
            switch (lexer.token()) {
                case KW_FOR:
                    lexer.nextToken();
                    match(MySQLToken.KW_UPDATE);
                    option.lockMode = DMLSelectStatement.LockMode.FOR_UPDATE;
                    break;
                case KW_LOCK:
                    lexer.nextToken();
                    match(MySQLToken.KW_IN);
                    matchIdentifier("SHARE");
                    matchIdentifier("MODE");
                    option.lockMode = DMLSelectStatement.LockMode.LOCK_IN_SHARE_MODE;
                    break;
            }
        }
        return new DMLSelectStatement(option, exprList, tables, where, group, having, order, limit);
    }

    /**
     * first token is either {@link MySQLToken#KW_SELECT} or
     * {@link MySQLToken#PUNC_LEFT_PAREN} which has been scanned but not yet
     * consumed
     *
     * @return {@link DMLSelectStatement} or {@link DMLSelectUnionStatement}
     */
    public DMLQueryStatement selectUnion() throws SQLSyntaxErrorException {
        DMLSelectStatement select = selectPrimary();
        DMLQueryStatement query = buildUnionSelect(select);
        return query;
    }

    private static enum SpecialIdentifier {
        SQL_BUFFER_RESULT,
        SQL_CACHE,
        SQL_NO_CACHE
    }

}
