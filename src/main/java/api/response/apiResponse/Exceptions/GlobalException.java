package api.response.apiResponse.Exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatusCode;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class GlobalException {
    private final String message;
    private final HttpStatusCode httpStatusCode;
    private final ZonedDateTime timestamp;
}
