/**
 * (created at 2011-6-17)
 */
package parse.recognizer;

import parse.ast.stmt.SQLStatement;
import parse.ast.stmt.ddl.DDLCreateIndexStatement;
import parse.ast.stmt.ddl.DDLStatement;
import parse.recognizer.mysql.MySQLToken;
import parse.recognizer.mysql.lexer.MySQLLexer;
import parse.recognizer.mysql.syntax.*;

import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.Map;

/**

 */
public final class SQLParserDelegate {
    private static final Map<String, SpecialIdentifier> specialIdentifiers = new HashMap<String, SpecialIdentifier>();

    static {
        specialIdentifiers.put("TRUNCATE", SpecialIdentifier.TRUNCATE);
        specialIdentifiers.put("SAVEPOINT", SpecialIdentifier.SAVEPOINT);
        specialIdentifiers.put("ROLLBACK", SpecialIdentifier.ROLLBACK);
        specialIdentifiers.put("BEGIN", SpecialIdentifier.BEGIN);
        specialIdentifiers.put("COMMIT", SpecialIdentifier.COMMIT);
    }

    private static boolean isEOFedDDL(SQLStatement stmt) {
        if (stmt instanceof DDLStatement) {
            if (stmt instanceof DDLCreateIndexStatement) return false;
        }
        return true;
    }

    private static String buildErrorMsg(Exception e, MySQLLexer lexer, String sql) {
        StringBuilder sb =
                new StringBuilder("You have an error in your SQL syntax; Error occurs around this fragment: ");
        final int ch = lexer.getCurrentIndex();
        int from = ch - 16;
        if (from < 0) from = 0;
        int to = ch + 9;
        if (to >= sql.length()) to = sql.length() - 1;
        String fragment = sql.substring(from, to + 1);
        sb.append('{').append(fragment).append('}').append(". Error cause: " + e.getMessage());
        return sb.toString();
    }

    public static SQLStatement parse(String sql, MySQLLexer lexer, String charset) throws SQLSyntaxErrorException {
        try {
            SQLStatement stmt = null;
            boolean isEOF = true;
            MySQLExprParser exprParser = new MySQLExprParser(lexer, charset);
            stmtSwitch:
            switch (lexer.token()) {
                case KW_DESC:
                case KW_DESCRIBE:
                    stmt = new MySQLDALParser(lexer, exprParser).desc();
                    break stmtSwitch;
                case KW_SELECT:
                case PUNC_LEFT_PAREN:
                    stmt = new MySQLDMLSelectParser(lexer, exprParser).selectUnion();
                    break stmtSwitch;
                case KW_DELETE:
                    stmt = new MySQLDMLDeleteParser(lexer, exprParser).delete();
                    break stmtSwitch;
                case KW_INSERT:
                    stmt = new MySQLDMLInsertParser(lexer, exprParser).insert();
                    break stmtSwitch;
                case KW_REPLACE:
                    stmt = new MySQLDMLReplaceParser(lexer, exprParser).replace();
                    break stmtSwitch;
                case KW_UPDATE:
                    stmt = new MySQLDMLUpdateParser(lexer, exprParser).update();
                    break stmtSwitch;
                case KW_CALL:
                    stmt = new MySQLDMLCallParser(lexer, exprParser).call();
                    break stmtSwitch;
                case KW_SET:
                    stmt = new MySQLDALParser(lexer, exprParser).set();
                    break stmtSwitch;
                case KW_SHOW:
                    stmt = new MySQLDALParser(lexer, exprParser).show();
                    break stmtSwitch;
                case KW_ALTER:
                case KW_CREATE:
                case KW_DROP:
                case KW_RENAME:
                    stmt = new MySQLDDLParser(lexer, exprParser).ddlStmt();
                    isEOF = isEOFedDDL(stmt);
                    break stmtSwitch;
                case KW_RELEASE:
                    stmt = new MySQLMTSParser(lexer).release();
                    break stmtSwitch;
                case KW_KILL:
                    stmt = new MySQLKillParser(lexer).kill();
                    break stmtSwitch;
                case KW_USE:
                    stmt = new MySQLUseParser(lexer).use();
                    break stmtSwitch;
                case IDENTIFIER:
                    SpecialIdentifier si = null;
                    if ((si = specialIdentifiers.get(lexer.stringValueUppercase())) != null) {
                        switch (si) {
                            case TRUNCATE:
                                stmt = new MySQLDDLParser(lexer, exprParser).truncate();
                                break stmtSwitch;
                            case SAVEPOINT:
                                stmt = new MySQLMTSParser(lexer).savepoint();
                                break stmtSwitch;
                            case ROLLBACK:
                                stmt = new MySQLMTSParser(lexer).rollback();
                                break stmtSwitch;
                            case BEGIN:
                                stmt = new MySQLMTSParser(lexer).begin();
                                break stmtSwitch;
                            case COMMIT:
                                stmt = new MySQLMTSParser(lexer).commit();
                                break stmtSwitch;
                        }
                    }
                default:
                    throw new SQLSyntaxErrorException("sql is not a supported statement");
            }
            if (isEOF) {
                while (lexer.token() == MySQLToken.PUNC_SEMICOLON) {
                    lexer.nextToken();
                }
                if (lexer.token() != MySQLToken.EOF) {
                    throw new SQLSyntaxErrorException("SQL syntax error!");
                }
            }
            return stmt;
        } catch (Exception e) {
            throw new SQLSyntaxErrorException(buildErrorMsg(e, lexer, sql), e);
        }
    }

    public static SQLStatement parse(StringBuilder sql, String charset) throws SQLSyntaxErrorException {
        return parse(sql.toString(), new MySQLLexer(sql.toString()), charset);
    }

    public static SQLStatement parse(StringBuilder sql) throws SQLSyntaxErrorException {
        return parse(sql, MySQLParser.DEFAULT_CHARSET);
    }

    private static enum SpecialIdentifier {
        ROLLBACK,
        SAVEPOINT,
        TRUNCATE,
        BEGIN,
        COMMIT
    }
}
