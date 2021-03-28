package amata1219.regex.permission.replacer.operation.record;

import amata1219.regex.permission.replacer.operation.OperationId;

public class UndoOperationRecord extends OperationRecord {

    public final OperationRecord operationUndone;

    public UndoOperationRecord(OperationId id, OperationRecord operationUndone) {
        super(id);
        this.operationUndone = operationUndone;
    }

}
