/**
 * (created at 2011-9-12)
 */
package parse.recognizer.mysql.syntax;

import parse.ast.expression.primary.Identifier;
import parse.ast.stmt.mts.*;
import parse.recognizer.mysql.lexer.MySQLLexer;
import parse.recognizer.mysql.MySQLToken;

import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.Map;

/**

 */
public class MySQLMTSParser extends MySQLParser {
    private static final Map<String, SpecialIdentifier> specialIdentifiers = new HashMap<String, SpecialIdentifier>();

    static {
        specialIdentifiers.put("SAVEPOINT", SpecialIdentifier.SAVEPOINT);
        specialIdentifiers.put("WORK", SpecialIdentifier.WORK);
        specialIdentifiers.put("CHAIN", SpecialIdentifier.CHAIN);
        specialIdentifiers.put("RELEASE", SpecialIdentifier.RELEASE);
        specialIdentifiers.put("NO", SpecialIdentifier.NO);
    }

    public MySQLMTSParser(MySQLLexer lexer) {
        super(lexer);
    }

    /**
     * first token <code>SAVEPOINT</code> is scanned but not yet consumed
     */
    public MTSSavepointStatement savepoint() throws SQLSyntaxErrorException {
        // matchIdentifier("SAVEPOINT"); // for performance issue, change to follow:
        lexer.nextToken();
        Identifier id = identifier();
        match(MySQLToken.EOF);
        return new MTSSavepointStatement(id);
    }

    /**
     * first token <code>RELEASE</code> is scanned but not yet consumed
     */
    public MTSReleaseStatement release() throws SQLSyntaxErrorException {
        match(MySQLToken.KW_RELEASE);
        matchIdentifier("SAVEPOINT");
        Identifier id = identifier();
        match(MySQLToken.EOF);
        return new MTSReleaseStatement(id);
    }

    /**
     * first token <code>ROLLBACK</code> is scanned but not yet consumed
     *
     * <pre>
     *         ROLLBACK [WORK] TO [SAVEPOINT] identifier
     *         ROLLBACK [WORK] [AND [NO] CHAIN | [NO] RELEASE]
     * </pre>
     */
    public MTSRollbackStatement rollback() throws SQLSyntaxErrorException {
        //matchIdentifier("ROLLBACK"); // for performance issue, change to follow:
        lexer.nextToken();
        SpecialIdentifier siTemp = specialIdentifiers.get(lexer.stringValueUppercase());
        if (siTemp == SpecialIdentifier.WORK) {
            lexer.nextToken();
        }
        switch (lexer.token()) {
            case EOF:
                return new MTSRollbackStatement(MTSRollbackStatement.CompleteType.UN_DEF);
            case KW_TO:
                lexer.nextToken();
                String str = lexer.stringValueUppercase();
                if (specialIdentifiers.get(str) == SpecialIdentifier.SAVEPOINT) {
                    lexer.nextToken();
                }
                Identifier savepoint = identifier();
                match(MySQLToken.EOF);
                return new MTSRollbackStatement(savepoint);
            case KW_AND:
                lexer.nextToken();
                siTemp = specialIdentifiers.get(lexer.stringValueUppercase());
                if (siTemp == SpecialIdentifier.NO) {
                    lexer.nextToken();
                    matchIdentifier("CHAIN");
                    match(MySQLToken.EOF);
                    return new MTSRollbackStatement(MTSRollbackStatement.CompleteType.NO_CHAIN);
                }
                matchIdentifier("CHAIN");
                match(MySQLToken.EOF);
                return new MTSRollbackStatement(MTSRollbackStatement.CompleteType.CHAIN);
            case KW_RELEASE:
                lexer.nextToken();
                match(MySQLToken.EOF);
                return new MTSRollbackStatement(MTSRollbackStatement.CompleteType.RELEASE);
            case IDENTIFIER:
                siTemp = specialIdentifiers.get(lexer.stringValueUppercase());
                if (siTemp == SpecialIdentifier.NO) {
                    lexer.nextToken();
                    match(MySQLToken.KW_RELEASE);
                    match(MySQLToken.EOF);
                    return new MTSRollbackStatement(MTSRollbackStatement.CompleteType.NO_RELEASE);
                }
            default:
                throw err("unrecognized complete type: " + lexer.token());
        }
    }

    public MTSBeginStatement begin() throws SQLSyntaxErrorException {
        lexer.nextToken();
        return new MTSBeginStatement();
    }

    public MTSCommitStatement commit() throws SQLSyntaxErrorException {
        lexer.nextToken();
        return new MTSCommitStatement();
    }


    private static enum SpecialIdentifier {
        CHAIN,
        NO,
        RELEASE,
        SAVEPOINT,
        WORK
    }

}
