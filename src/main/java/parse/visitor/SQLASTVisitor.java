/**
 * (created at 2011-5-30)
 */
package parse.visitor;

import parse.ast.expression.BinaryOperatorExpression;
import parse.ast.expression.PolyadicOperatorExpression;
import parse.ast.expression.UnaryOperatorExpression;
import parse.ast.expression.comparison.*;
import parse.ast.expression.logical.LogicalAndExpression;
import parse.ast.expression.logical.LogicalOrExpression;
import parse.ast.expression.misc.InExpressionList;
import parse.ast.expression.misc.UserExpression;
import parse.ast.expression.primary.function.FunctionExpression;
import parse.ast.expression.primary.function.cast.Cast;
import parse.ast.expression.primary.function.cast.Convert;
import parse.ast.expression.primary.function.datetime.Extract;
import parse.ast.expression.primary.function.datetime.GetFormat;
import parse.ast.expression.primary.function.datetime.Timestampadd;
import parse.ast.expression.primary.function.datetime.Timestampdiff;
import parse.ast.expression.primary.function.groupby.*;
import parse.ast.expression.primary.function.string.Char;
import parse.ast.expression.primary.function.string.Trim;
import parse.ast.expression.primary.literal.*;
import parse.ast.expression.string.LikeExpression;
import parse.ast.expression.type.CollateExpression;
import parse.ast.fragment.GroupBy;
import parse.ast.fragment.Limit;
import parse.ast.fragment.OrderBy;
import parse.ast.fragment.ddl.ColumnDefinition;
import parse.ast.fragment.ddl.TableOptions;
import parse.ast.fragment.ddl.datatype.DataType;
import parse.ast.fragment.ddl.index.IndexColumnName;
import parse.ast.fragment.ddl.index.IndexOption;
import parse.ast.fragment.tableref.*;
import parse.ast.stmt.ddl.DDLAlterTableStatement.AlterSpecification;
import parse.ast.stmt.extension.ExtDDLCreatePolicy;
import parse.ast.stmt.extension.ExtDDLDropPolicy;
import parse.ast.stmt.mts.MTSReleaseStatement;
import parse.ast.stmt.mts.MTSRollbackStatement;
import parse.ast.stmt.mts.MTSSavepointStatement;
import parse.ast.stmt.mts.MTSSetTransactionStatement;
import parse.ast.expression.primary.*;
import parse.ast.stmt.dal.*;
import parse.ast.stmt.ddl.*;
import parse.ast.stmt.dml.*;

/**

 */
public interface SQLASTVisitor {

    void visit(BetweenAndExpression node);

    void visit(ComparisionIsExpression node);

    void visit(InExpressionList node);

    void visit(LikeExpression node);

    void visit(CollateExpression node);

    void visit(UserExpression node);

    void visit(UnaryOperatorExpression node);

    void visit(BinaryOperatorExpression node);

    void visit(PolyadicOperatorExpression node);

    void visit(LogicalAndExpression node);

    void visit(LogicalOrExpression node);

    void visit(ComparisionEqualsExpression node);

    void visit(ComparisionNullSafeEqualsExpression node);

    void visit(InExpression node);

    //-------------------------------------------------------
    void visit(FunctionExpression node);

    void visit(Char node);

    void visit(Convert node);

    void visit(Trim node);

    void visit(Cast node);

    void visit(Avg node);

    void visit(Max node);

    void visit(Min node);

    void visit(Sum node);

    void visit(Count node);

    void visit(GroupConcat node);

    void visit(Extract node);

    void visit(Timestampdiff node);

    void visit(Timestampadd node);

    void visit(GetFormat node);

    //-------------------------------------------------------
    void visit(IntervalPrimary node);

    void visit(LiteralBitField node);

    void visit(LiteralBoolean node);

    void visit(LiteralHexadecimal node);

    void visit(LiteralNull node);

    void visit(LiteralNumber node);

    void visit(LiteralString node);

    void visit(CaseWhenOperatorExpression node);

    void visit(DefaultValue node);

    void visit(ExistsPrimary node);

    void visit(PlaceHolder node);

    void visit(Identifier node);

    void visit(MatchExpression node);

    void visit(ParamMarker node);

    void visit(RowExpression node);

    void visit(SysVarPrimary node);

    void visit(UsrDefVarPrimary node);

    //-------------------------------------------------------
    void visit(IndexHint node);

    void visit(InnerJoin node);

    void visit(NaturalJoin node);

    void visit(OuterJoin node);

    void visit(StraightJoin node);

    void visit(SubqueryFactor node);

    void visit(TableReferences node);

    void visit(TableRefFactor node);

    void visit(Dual dual);

    void visit(GroupBy node);

    void visit(Limit node);

    void visit(OrderBy node);

    void visit(ColumnDefinition node);

    void visit(IndexOption node);

    void visit(IndexColumnName node);

    void visit(TableOptions node);

    void visit(AlterSpecification node);

    void visit(DataType node);

    //-------------------------------------------------------
    void visit(ShowAuthors node);

    void visit(ShowBinaryLog node);

    void visit(ShowBinLogEvent node);

    void visit(ShowCharaterSet node);

    void visit(ShowCollation node);

    void visit(ShowColumns node);

    void visit(ShowContributors node);

    void visit(ShowCreate node);

    void visit(ShowDatabases node);

    void visit(ShowEngine node);

    void visit(ShowEngines node);

    void visit(ShowErrors node);

    void visit(ShowEvents node);

    void visit(ShowFunctionCode node);

    void visit(ShowFunctionStatus node);

    void visit(ShowGrants node);

    void visit(ShowIndex node);

    void visit(ShowMasterStatus node);

    void visit(ShowOpenTables node);

    void visit(ShowPlugins node);

    void visit(ShowPrivileges node);

    void visit(ShowProcedureCode node);

    void visit(ShowProcedureStatus node);

    void visit(ShowProcesslist node);

    void visit(ShowProfile node);

    void visit(ShowProfiles node);

    void visit(ShowSlaveHosts node);

    void visit(ShowSlaveStatus node);

    void visit(ShowStatus node);

    void visit(ShowTables node);

    void visit(ShowTableStatus node);

    void visit(ShowTriggers node);

    void visit(ShowVariables node);

    void visit(ShowWarnings node);

    void visit(DescTableStatement node);

    void visit(DALSetStatement node);

    void visit(DALSetNamesStatement node);

    void visit(DALSetCharacterSetStatement node);

    //-------------------------------------------------------
    void visit(DMLCallStatement node);

    void visit(DMLDeleteStatement node);

    void visit(DMLInsertStatement node);

    void visit(DMLReplaceStatement node);

    void visit(DMLSelectStatement node);

    void visit(DMLSelectUnionStatement node);

    void visit(DMLUpdateStatement node);

    void visit(MTSSetTransactionStatement node);

    void visit(MTSSavepointStatement node);

    void visit(MTSReleaseStatement node);

    void visit(MTSRollbackStatement node);

    void visit(DDLTruncateStatement node);

    void visit(DDLAlterTableStatement node);

    void visit(DDLCreateIndexStatement node);

    void visit(DDLCreateTableStatement node);

    void visit(DDLRenameTableStatement node);

    void visit(DDLDropIndexStatement node);

    void visit(DDLDropTableStatement node);

    void visit(ExtDDLCreatePolicy node);

    void visit(ExtDDLDropPolicy node);

    void visit(ShowFields node);

}
