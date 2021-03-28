package amata1219.regex.permission.replacer;

import java.util.HashSet;
import java.util.Objects;

public class OperationId implements Comparable<OperationId> {

    static final HashSet<Long> ISSUED_OPERATION_IDS = new HashSet<>();

    private final long value;

    public OperationId(long value) {
        if (!valueIsIssuedOperationId(value)) throw new IllegalArgumentException("value must be issued operation id");
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OperationId that = (OperationId) obj;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(OperationId other) {
        return Long.compare(value, other.value);
    }

    @Override
    public String toString() {
        return "OperationId{" +
                "value=" + value +
                '}';
    }

    public static OperationId issueNewOperationId() {
        final long value = System.currentTimeMillis();
        ISSUED_OPERATION_IDS.add(value);
        return new OperationId(value);
    }

    public static boolean valueIsIssuedOperationId(long value) {
        return ISSUED_OPERATION_IDS.contains(value);
    }

}
