package amata1219.regex.permission.replacer.operation.record;

import amata1219.regex.permission.replacer.OperationType;

public class RedoOperationRecord extends OperationRecord {

    public final OperationRecord operationRedone;

    public RedoOperationRecord(long id, OperationType operationType, OperationRecord operationUndone) {
        super(id, operationType);
        this.operationRedone = operationUndone;
    }

}