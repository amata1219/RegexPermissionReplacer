package amata1219.regex.permission.replacer.operation.record;

import amata1219.regex.permission.replacer.OperationId;

import java.util.TreeMap;

public class OperationRecords {

    public final TreeMap<OperationId, OperationRecord> operationRecords;

    public OperationRecords(TreeMap<OperationId, OperationRecord> operationRecords) {
        this.operationRecords = operationRecords;
    }

    public void put(OperationRecord record) {
        operationRecords.put(record.id, record);
    }

}
