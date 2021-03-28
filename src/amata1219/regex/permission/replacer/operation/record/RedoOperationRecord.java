package amata1219.regex.permission.replacer.operation.record;

import amata1219.regex.permission.replacer.operation.OperationId;

public class RedoOperationRecord extends OperationRecord {

    public final OperationRecord operationRedone;

    public RedoOperationRecord(OperationId id, OperationRecord operationUndone) {
        super(id);
        this.operationRedone = operationUndone;
    }

}