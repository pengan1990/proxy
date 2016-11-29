package parse.recognizer.mysql.syntax;

import parse.ast.stmt.SQLStatement;
import parse.ast.stmt.dml.DMLKIllStatement;
import parse.recognizer.mysql.MySQLToken;
import parse.recognizer.mysql.lexer.MySQLLexer;

import java.sql.SQLSyntaxErrorException;

/**
 * Created by pengan on 16-11-4.
 */
public class MySQLKillParser extends MySQLParser {

    public MySQLKillParser(MySQLLexer lexer) {
        super(lexer);
    }

    public SQLStatement kill() throws SQLSyntaxErrorException {
        match(MySQLToken.KW_KILL);
        long connectionId = 0L;
        switch (lexer.token()) {
            case KW_QUERY:
                lexer.nextToken();
                connectionId = assembleId();
                lexer.nextToken();
                break;
            case LITERAL_NUM_PURE_DIGIT:
                connectionId = assembleId();
                lexer.nextToken();
                break;
            default:
                throw err("expect id or * after '.'");
        }
        return new DMLKIllStatement(connectionId);
    }

    private long assembleId() {
        StringBuilder killId = new StringBuilder();
        for (int index = lexer.getCurrentIndex()  - lexer.getSizeCache();
             index < lexer.getCurrentIndex(); index ++) {
            killId.append(lexer.getSQL()[index]);
        }
        return Long.parseLong(killId.toString().trim());
    }
}
