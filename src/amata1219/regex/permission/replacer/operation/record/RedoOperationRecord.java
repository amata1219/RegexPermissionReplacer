package amata1219.regex.permission.replacer.operation.record;

public class RedoOperationRecord extends OperationRecord {

    public final OperationRecord operationRedone;

    public RedoOperationRecord(long id, OperationRecord operationUndone) {
        super(id);
        this.operationRedone = operationUndone;
    }

}