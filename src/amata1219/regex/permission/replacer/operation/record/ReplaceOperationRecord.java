package amata1219.regex.permission.replacer.operation.record;

import amata1219.regex.permission.replacer.operation.OperationId;
import amata1219.regex.permission.replacer.operation.target.Target;

public class ReplaceOperationRecord extends OperationRecord {

    public final String regex, replacement;
    public final Target target;

    public ReplaceOperationRecord(OperationId id, String regex, String replacement, Target target) {
        super(id);
        this.regex = regex;
        this.replacement = replacement;
        this.target = target;
    }

}