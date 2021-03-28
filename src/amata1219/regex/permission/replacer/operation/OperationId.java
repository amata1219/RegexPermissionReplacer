package amata1219.regex.permission.replacer.operation;

import java.util.Objects;

public class OperationId implements Comparable<OperationId> {

    private final long value;

    public OperationId(long value) {
        this.value = value;
    }

    @Override
    public int compareTo(OperationId other) {
        return Long.compare(value, other.value);
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

}
