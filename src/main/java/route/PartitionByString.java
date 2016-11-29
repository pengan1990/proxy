package route;

import config.model.rule.RuleAlgorithm;
import util.PartitionUtil;
import util.StringUtil;

import java.util.Map;

/**
 * Created by pengan on 16-10-17.
 */
public class PartitionByString implements RuleAlgorithm {
    private int count;
    private int length;
    protected PartitionUtil partitionUtil;

    public PartitionByString() {
    }

    public PartitionByString(Object... objects) {
        this.count = Integer.parseInt(objects[0] + "");
        this.length = Integer.parseInt(objects[1] + "");
    }

    @Override
    public RuleAlgorithm constructMe(Object... objects) {
        PartitionByString partition = new PartitionByString(objects);
        return partition;
    }

    @Override
    public void initialize() {
        int[] counts = new int[count];
        int[] lengths = new int[count];
        for (int index = 0; index < count; index++) {
            counts[index] = 1;
            lengths[index] = length;
        }
        partitionUtil = new PartitionUtil(counts, lengths);
    }

    @Override
    public Integer[] calculate(Map<? extends Object, ? extends Object> parameters) {
        Integer[] rst = new Integer[1];
        if (parameters.size() == 0 || parameters.size() != 1) {
            throw new IllegalArgumentException("parameters is not compatible for PartitionByString");
        }
        StringBuilder colVal = new StringBuilder();
        Object val = parameters.values().iterator().next();
        if (val == null || "".equals(val)) {
            throw new IllegalArgumentException("character value cannot be null");
        }
        colVal.append(val);
        long hash = StringUtil.hash(colVal.toString(), 0, colVal.length());
        rst[0] = partitionIndex(hash);
        return rst;
    }


    private int partitionIndex(long hash) {
        return partitionUtil.partition(hash);
    }
}
