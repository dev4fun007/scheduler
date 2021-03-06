package bytes.sync.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCronExpression extends RuntimeException {
    public InvalidCronExpression(String message) {
        super(message);
    }
}
