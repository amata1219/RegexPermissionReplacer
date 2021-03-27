package amata1219.regex.permission.replacer.operation.record;

import amata1219.regex.permission.replacer.OperationType;

public class UndoOperationRecord extends OperationRecord {

    public final OperationRecord operationUndone;

    public UndoOperationRecord(long id, OperationType operationType, OperationRecord operationUndone) {
        super(id, operationType);
        this.operationUndone = operationUndone;
    }

}
