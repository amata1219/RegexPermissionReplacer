package amata1219.regex.permission.replacer.operation.record;

import amata1219.regex.permission.replacer.OperationType;

public abstract class OperationRecord {

    public final long id;
    public final OperationType operationType;

    public OperationRecord(long id, OperationType operationType) {
        this.id = id;
        this.operationType = operationType;
    }

}
