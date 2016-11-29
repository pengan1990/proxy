/**
 * (created at 2011-5-19)
 */
package parse.recognizer.mysql.syntax;

import parse.ast.expression.Expression;
import parse.ast.expression.primary.Identifier;
import parse.ast.stmt.dml.DMLCallStatement;
import parse.recognizer.mysql.lexer.MySQLLexer;
import parse.recognizer.mysql.MySQLToken;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**

 */
public class MySQLDMLCallParser extends MySQLDMLParser {
    public MySQLDMLCallParser(MySQLLexer lexer, MySQLExprParser exprParser) {
        super(lexer, exprParser);
    }

    public DMLCallStatement call() throws SQLSyntaxErrorException {
        match(MySQLToken.KW_CALL);
        Identifier procedure = identifier();
        match(MySQLToken.PUNC_LEFT_PAREN);
        if (lexer.token() == MySQLToken.PUNC_RIGHT_PAREN) {
            lexer.nextToken();
            return new DMLCallStatement(procedure);
        }
        List<Expression> arguments;
        Expression expr = exprParser.expression();
        switch (lexer.token()) {
            case PUNC_COMMA:
                arguments = new LinkedList<Expression>();
                arguments.add(expr);
                for (; lexer.token() == MySQLToken.PUNC_COMMA; ) {
                    lexer.nextToken();
                    expr = exprParser.expression();
                    arguments.add(expr);
                }
                match(MySQLToken.PUNC_RIGHT_PAREN);
                return new DMLCallStatement(procedure, arguments);
            case PUNC_RIGHT_PAREN:
                lexer.nextToken();
                arguments = new ArrayList<Expression>(1);
                arguments.add(expr);
                return new DMLCallStatement(procedure, arguments);
            default:
                throw err("expect ',' or ')' after first argument of procedure");
        }
    }

}
