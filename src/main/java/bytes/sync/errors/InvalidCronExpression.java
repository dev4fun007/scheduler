package bytes.sync.errors;

public class InvalidCronExpression extends RuntimeException {
    public InvalidCronExpression(String message) {
        super(message);
    }
}
