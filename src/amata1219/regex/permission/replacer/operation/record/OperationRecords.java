package amata1219.regex.permission.replacer.operation.record;

import amata1219.regex.permission.replacer.OperationId;
import amata1219.regex.permission.replacer.config.MainConfig;

import java.util.TreeMap;

public class OperationRecords {

    private final MainConfig config;
    public final TreeMap<OperationId, OperationRecord> operationRecords;
    private final TreeMap<OperationId, ReplaceOperationRecord> replaceOperationRecords;

    public OperationRecords(MainConfig config, TreeMap<OperationId, OperationRecord> operationRecords, TreeMap<OperationId, ReplaceOperationRecord> replaceOperationRecords) {
        this.config = config;
        this.operationRecords = operationRecords;
        this.replaceOperationRecords = replaceOperationRecords;
    }

    public void put(OperationRecord record) {
        operationRecords.put(record.id, record);
        if (record instanceof ReplaceOperationRecord) replaceOperationRecords.put(record.id, (ReplaceOperationRecord) record);
    }

    public ReplaceOperationRecord lastReplaceOperationRecord() {
        return replaceOperationRecords.get(replaceOperationRecords.firstKey());
    }

}
