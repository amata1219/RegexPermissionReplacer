package amata1219.regex.permission.replacer.operation.record;

import amata1219.regex.permission.replacer.OperationType;

public class ReplaceOperaationRecord extends OperationRecord {

    public final String regex, replacement;

    public ReplaceOperaationRecord(long id, OperationType operationType, String regex, String replacement) {
        super(id, operationType);
        this.regex = regex;
        this.replacement = replacement;
    }

}