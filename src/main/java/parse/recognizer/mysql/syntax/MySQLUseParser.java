package parse.recognizer.mysql.syntax;

import parse.ast.expression.primary.Identifier;
import parse.ast.stmt.SQLStatement;
import parse.ast.stmt.dml.DMLUseStatement;
import parse.recognizer.mysql.MySQLToken;
import parse.recognizer.mysql.lexer.MySQLLexer;

import java.sql.SQLSyntaxErrorException;

/**
 * Created by pengan on 16-11-4.
 */
public class MySQLUseParser extends MySQLParser {

    public MySQLUseParser(MySQLLexer lexer) {
        super(lexer);
    }

    public SQLStatement use() throws SQLSyntaxErrorException {
        match(MySQLToken.KW_USE);
        Identifier schema;
        switch (lexer.nextToken()) {
            case IDENTIFIER:
                schema = new Identifier(null, lexer.stringValue(), lexer.stringValueUppercase());
                schema.setCacheEvalRst(cacheEvalRst);
                lexer.nextToken();
                break;
            default:
                throw err("expect id or * after '.'");
        }
        return new DMLUseStatement(schema);
    }
}
