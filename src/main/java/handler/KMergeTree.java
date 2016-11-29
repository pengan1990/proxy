package handler;

import conn.NonBlockSession;
import mysql.FieldPacket;
import mysql.RowDataPacket;
import org.apache.log4j.Logger;
import parse.ast.fragment.SortOrder;
import util.CompareUtil;
import util.FieldType;

import java.util.*;

/**
 * Created by pengan on 16-11-9.
 */
public class KMergeTree {
    private static final Logger logger = Logger.getLogger(KMergeTree.class);
    public static final RowDataPacket LOSER = new RowDataPacket(0);
    private static final RowDataPacket WINNER = new RowDataPacket(0);
    private static final int DEFAULT_WINNER_LEAF_POSITION = -1;
    private boolean isBuild;
    private int winnerLeafPos;
    private int K;
    private int sendRowNumber;
    private int[] tree;
    private RowDataPacket sendRow; // only for group by to store row data packet temporary
    private List<RowDataPacket>[] leaves;
    private NonBlockSession session;

    public KMergeTree(NonBlockSession session) {
        this.session = session;
    }

    /**
     * init KMergeTree parameters
     */
    public void init(int nodeNumber) {
        logger.debug("init");
        sendRowNumber = 0;
        K = nodeNumber;
        this.tree = new int[K];
        this.leaves = new List[K + 1];
        int index = 0;
        for (List<RowDataPacket> kRow : session.getHandler().getBackRows()) {
            this.leaves[index++] = kRow;
        }
        List<RowDataPacket> minColumn = new LinkedList<RowDataPacket>();
        minColumn.add(WINNER);
        this.leaves[index] = minColumn;
        this.isBuild = false;
    }

    /**
     * one round to call @reset
     */
    public void reset() {
        logger.debug("reset");
        this.winnerLeafPos = DEFAULT_WINNER_LEAF_POSITION;
        this.sendRowNumber = 0;
        this.isBuild = false;
        this.K = 0;
        this.sendRow = null;
        this.leaves = null; // row.clear() call in SessionHandler.reset()
        this.tree = null;
    }

    private int compare(RowDataPacket left, RowDataPacket right) throws Exception {
        logger.debug("compare");
        if (left == LOSER && right == LOSER) {
            return 0;
        }
        if (right == LOSER) {
            return 1;
        }
        if (left == LOSER) {
            return -1;
        }

        Map<Integer, SortOrder> orderByColumnSet = session.getRouteHint().getOrderByColumns();
        List<FieldPacket> fieldPackets = session.getHandler().getFields();
        int columnPosition;
        int rst = 0;
        for (Map.Entry<Integer, SortOrder> entry : orderByColumnSet.entrySet()) {
            columnPosition = entry.getKey();
            rst = CompareUtil.compare(left.fieldValues.get(columnPosition),
                    right.fieldValues.get(columnPosition), fieldPackets.get(columnPosition).type);
            switch (entry.getValue()) {
                case ASC:
                    break;
                case DESC:
                    rst = -rst;
                    break;
            }
            if (rst != 0) {
                return rst;
            }
        }
        return rst; // here to be zero
    }

    /**
     * failure to reserve
     *
     * @param leafPos
     */
    private void adjust(int leafPos) throws Exception {
        logger.debug("adjust");
        int parentPos = ((leafPos + tree.length) >> 1);
        int tmp;
        while (parentPos > 0) {
            if (leaves[leafPos].get(0) == WINNER) {
                // child == winner
                break;
            }
            // TODO: 16-11-20 这块代码风格需要调整
            if (leaves[tree[parentPos]].get(0) == WINNER ||
                    leaves[leafPos].get(0) == LOSER) {
                tmp = leafPos;
                leafPos = tree[parentPos];
                tree[parentPos] = tmp;
            } else {
                if (leaves[tree[parentPos]].get(0) == LOSER) {
                    // if parent == loser then
                } else if (compare(leaves[leafPos].get(0), leaves[tree[parentPos]].get(0)) > 0) {
                    tmp = leafPos;
                    leafPos = tree[parentPos];
                    tree[parentPos] = tmp;
                }
            }
            parentPos = (parentPos >> 1);
        }
        tree[0] = leafPos;
    }

    /**
     * 原因： @kMerge 的时候 可能由于row.size()==0 出现退出loop
     * <p/>
     * row eof (row.lastIndex() == LOSER)调用 @kMerge 无法继续排序
     *
     * @throws Exception
     */
    public void refresh() throws Exception {
        logger.debug("refresh");
        if (winnerLeafPos == DEFAULT_WINNER_LEAF_POSITION) {
            return;
        }
        adjust(winnerLeafPos);
    }

    public void build() throws Exception {
        logger.debug("build");
        Arrays.fill(tree, K);

        for (int leafPos = K - 1; leafPos > -1; leafPos--) {
            adjust(leafPos);
        }
    }

    /**
     * break when the winner are the LOSER or one leaf node have not enough data
     */
    public void kMerge() throws Exception {
        logger.debug("kMerge");
        RouteHintHandler routeHint = session.getRouteHint();
        if (routeHint.getOrderByColumns().size() == 0) {
            // no order by => no group by & no order by & no limit
            // only one row in each channel
            if (sendRow != null) {
                return;
            }
            sendRow = leaves[0].remove(0);
            for (int leafPos = 1; leafPos < K; leafPos++) {
                merge(leaves[leafPos].remove(0));
            }
            sendRowDataPacket(sendRow);
            return;
        }

        if (!isBuild) {
            build();
            isBuild = true;
        }
        while (leaves[tree[0]].get(0) != LOSER) {
            winnerLeafPos = tree[0];
            if (leaves[winnerLeafPos].size() > 0) {
                sendRowDataPacket(leaves[winnerLeafPos].remove(0));
            }
            if (leaves[winnerLeafPos].size() == 0) {
                // break: if no data in buffer
                return; // go back to wait for enough data
            }
            adjust(winnerLeafPos);
        }
        if (sendRow != null) {
            sendRowDataPacket(sendRow);
        }
    }

    private boolean printTree() {
        String colVal = null;
        RowDataPacket row = null;
        for (int index = 0; index < tree.length; index++) {
            row = leaves[tree[index]].get(0);
            colVal = null;
            if (row == LOSER) {
                colVal = "loser";
            } else if (row == WINNER) {
                colVal = "winner";
            } else {
                colVal = new String(row.fieldValues.get(5));
            }
            System.err.print("[" + index + "]=" + tree[index] + "=" + colVal + ", ");
        }
        System.err.println();
        return true;
    }

    private void sendRowDataPacket(RowDataPacket row) throws Exception {
        logger.debug("sendRowDataPacket");
        RouteHintHandler routeHint = session.getRouteHint();
        SessionHandler handler = session.getHandler();
        if (routeHint.getGroupByColumnSet().size() > 0) {
            if (sendRow == null) {
                sendRow = row;
                return;
            }
            if (canMerge(row)) {
                boolean success = merge(row);
                if (success) {
                    return;
                }
            }
            // if cannot merge or merge failure
        }
        long[] limit = routeHint.getLimit();
        if (limit[0] != limit[1]) {
            if ((this.sendRowNumber >= limit[0]) && (this.sendRowNumber < limit[1])) {
                handler.write(row, routeHint.getDeleteColumnSet(), false);
            }
            // if not between limit offset sizeha
            sendRowNumber++;
            return;
        }
        handler.write(row, routeHint.getDeleteColumnSet(), false);
        sendRowNumber++; // row not send, counter add
    }

    private boolean merge(RowDataPacket row) {
        logger.debug("merge");
        RouteHintHandler routeHint = session.getRouteHint();
        List<FieldPacket> fieldPackets = session.getHandler().getFields();
        Set<Integer> groupByColumnSet = routeHint.getGroupByColumnSet();
        for (int column = 0; column < row.fieldCount; column++) {
            if (groupByColumnSet.contains(column)) {
                continue;
            }
            switch (routeHint.judgeColumnOperation(column)) {
                case MIN:
                    if (!handleMin(row, fieldPackets.get(column), column)) {
                        return false;
                    }
                    break;
                case MAX:
                    if (!handleMax(row, fieldPackets.get(column), column)) {
                        return false;
                    }
                    break;
                case COUNT:
                case SUM:
                    if (!handleSum(row, fieldPackets.get(column), column)) {
                        return false;
                    }
                    break;
                case AVG:
                    if (!handleAvg(row, fieldPackets.get(column), column)) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    private boolean handleAvg(RowDataPacket row, FieldPacket fieldPacket, int column) {
        logger.debug("handleAvg");
        RouteHintHandler routeHint = session.getRouteHint();
        Map<Integer, Integer> avgAddColumnMap = routeHint.getAvgAddColumns();
        int countColumn = avgAddColumnMap.get(column);
        switch (fieldPacket.type) {
            case FieldType.FIELD_TYPE_LONGLONG:
            case FieldType.FIELD_TYPE_LONG://int, int signed
            case FieldType.FIELD_TYPE_NEWDECIMAL://decimal numeric
            case FieldType.FIELD_TYPE_DOUBLE://real, double precision
            case FieldType.FIELD_TYPE_FLOAT://float
                Double avg1 = Double.parseDouble(new String(row.fieldValues.get(column)));
                Long count1 = Long.parseLong(new String(row.fieldValues.get(countColumn)));
                Double avg2 = Double.parseDouble(new String(sendRow.fieldValues.get(column)));
                Long count2 = Long.parseLong(new String(sendRow.fieldValues.get(countColumn)));
                Double avg = (avg1 * count1 + avg2 * count2);
                Long count = count1 + count2;
                if (count == 0) { // may be zero
                    avg = 0.0;
                } else {
                    avg = avg / count;
                }
                sendRow.fieldValues.set(column, avg.toString().getBytes());
                sendRow.fieldValues.set(countColumn, count.toString().getBytes());
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean handleSum(RowDataPacket row, FieldPacket fieldPacket, int column) {
        logger.debug("handleSum");
        switch (fieldPacket.type) {
            case FieldType.FIELD_TYPE_LONG://int, int signed
            case FieldType.FIELD_TYPE_LONGLONG:
                Long rstLong = Long.parseLong(new String(row.fieldValues.get(column))) +
                        Long.parseLong(new String(sendRow.fieldValues.get(column)));
                sendRow.fieldValues.set(column, rstLong.toString().getBytes());
                break;
            case FieldType.FIELD_TYPE_NEWDECIMAL://decimal numeric
            case FieldType.FIELD_TYPE_DOUBLE://real, double precision
            case FieldType.FIELD_TYPE_FLOAT://float
                Double rstDouble = Double.parseDouble(new String(row.fieldValues.get(column))) +
                        Double.parseDouble(new String(sendRow.fieldValues.get(column)));
                sendRow.fieldValues.set(column, rstDouble.toString().getBytes());
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean handleMax(RowDataPacket row, FieldPacket fieldPacket, int column) {
        logger.debug("handleMax");
        switch (fieldPacket.type) {
            case FieldType.FIELD_TYPE_LONGLONG:
            case FieldType.FIELD_TYPE_LONG://int, int signed
                Long rstLong = Math.max(Long.parseLong(new String(row.fieldValues.get(column))),
                        Long.parseLong(new String(sendRow.fieldValues.get(column))));
                sendRow.fieldValues.set(column, rstLong.toString().getBytes());
                break;
            case FieldType.FIELD_TYPE_NEWDECIMAL://decimal numeric
            case FieldType.FIELD_TYPE_DOUBLE://real, double precision
            case FieldType.FIELD_TYPE_FLOAT://float
                Double rstDouble = Math.max(Double.parseDouble(new String(row.fieldValues.get(column))),
                        Double.parseDouble(new String(sendRow.fieldValues.get(column))));
                sendRow.fieldValues.set(column, rstDouble.toString().getBytes());
                break;
            case FieldType.FIELD_TYPE_STRING:
            case FieldType.FIELD_TYPE_VARCHAR:
            case FieldType.FIELD_TYPE_VAR_STRING:
                try {
                    if (CompareUtil.compare(sendRow.fieldValues.get(column),
                            row.fieldValues.get(column), fieldPacket.type) < 0) {
                        sendRow.fieldValues.set(column, row.fieldValues.get(column));
                    }
                } catch (Exception e) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean handleMin(RowDataPacket row, FieldPacket fieldPacket, int fieldIndex) {
        logger.debug("handleMin");
        switch (fieldPacket.type) {
            case FieldType.FIELD_TYPE_LONGLONG:
            case FieldType.FIELD_TYPE_LONG://int, int signed
                Long rstLong = Math.min(Long.parseLong(new String(row.fieldValues.get(fieldIndex))),
                        Long.parseLong(new String(sendRow.fieldValues.get(fieldIndex))));
                sendRow.fieldValues.set(fieldIndex, rstLong.toString().getBytes());
                break;
            case FieldType.FIELD_TYPE_NEWDECIMAL://decimal numeric
            case FieldType.FIELD_TYPE_DOUBLE://real, double precision
            case FieldType.FIELD_TYPE_FLOAT://float
                Double rstDouble = Math.min(Double.parseDouble(new String(row.fieldValues.get(fieldIndex))),
                        Double.parseDouble(new String(sendRow.fieldValues.get(fieldIndex))));
                sendRow.fieldValues.set(fieldIndex, rstDouble.toString().getBytes());
                break;
            case FieldType.FIELD_TYPE_STRING:
            case FieldType.FIELD_TYPE_VARCHAR:
            case FieldType.FIELD_TYPE_VAR_STRING:
                try {
                    if (sendRow.fieldValues.get(fieldIndex) == null) {
                        sendRow.fieldValues.set(fieldIndex, row.fieldValues.get(fieldIndex));
                        break;
                    }
                    if (row.fieldValues.get(fieldIndex) != null &&
                            CompareUtil.compare(sendRow.fieldValues.get(fieldIndex),
                                    row.fieldValues.get(fieldIndex), fieldPacket.type) > 0) {
                        sendRow.fieldValues.set(fieldIndex, row.fieldValues.get(fieldIndex));
                    }
                } catch (Exception e) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean canMerge(RowDataPacket row) throws Exception {
        logger.debug("canMerge");
        RouteHintHandler routeHint = session.getRouteHint();
        List<FieldPacket> fieldPackets = session.getHandler().getFields();
        for (Integer column : routeHint.getGroupByColumnSet()) {
            if (CompareUtil.compare(row.fieldValues.get(column),
                    sendRow.fieldValues.get(column), fieldPackets.get(column).type) != 0) {
                return false;
            }
        }
        return true;
    }
}
