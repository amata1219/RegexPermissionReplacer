package amata1219.regex.permission.replacer.operation.record;

import amata1219.regex.permission.replacer.OperationType;
import amata1219.regex.permission.replacer.operation.target.Target;

public class ReplaceOperationRecord extends OperationRecord {

    public final String regex, replacement;
    public final Target target;

    public ReplaceOperationRecord(long id, OperationType operationType, String regex, String replacement, Target target) {
        super(id, operationType);
        this.regex = regex;
        this.replacement = replacement;
        this.target = target;
    }

}