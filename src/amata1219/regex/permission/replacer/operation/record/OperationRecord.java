package amata1219.regex.permission.replacer.operation.record;

import amata1219.regex.permission.replacer.operation.OperationId;

public abstract class OperationRecord {

    public final OperationId id;

    public OperationRecord(OperationId id) {
        this.id = id;
    }

}
