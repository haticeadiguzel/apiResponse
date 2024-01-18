package api.response.apiResponse.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {ApiRequestException.class})
    public ResponseEntity<Object> handleApiRequestException(ApiRequestException e) {
        HttpStatus httpStatus = HttpStatus.OK;

        GlobalException globalException = new GlobalException(
                e.getMessage(),
                httpStatus,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(globalException, httpStatus);
    }

    @ExceptionHandler(value = {WhoisResultRequestException.class})
    public ResponseEntity<Object> handleWhoisResultRequestException(WhoisResultRequestException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        GlobalException globalException = new GlobalException(
                e.getMessage(),
                httpStatus,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(globalException, httpStatus);
    }

    @ExceptionHandler(value = {ParseWhoisResultException.class})
    public ResponseEntity<Object> handleWhoisResultException(ParseWhoisResultException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        GlobalException globalException = new GlobalException(
                e.getMessage(),
                httpStatus,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(globalException, httpStatus);
    }

    @ExceptionHandler(value = {SaveWhoisResultToRedisException.class})
    public ResponseEntity<Object> saveWhoisResultToRedisException(SaveWhoisResultToRedisException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        GlobalException globalException = new GlobalException(
                e.getMessage(),
                httpStatus,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(globalException, httpStatus);
    }

    @ExceptionHandler(value = {GetTotalCountException.class})
    public ResponseEntity<Object> getTotalCountException(GetTotalCountException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        GlobalException globalException = new GlobalException(
                e.getMessage(),
                httpStatus,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(globalException, httpStatus);
    }

    @ExceptionHandler(value = {GetAllAddressesResponseException.class})
    public ResponseEntity<Object> getAllAddressesResponseException(GetAllAddressesResponseException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        GlobalException globalException = new GlobalException(
                e.getMessage(),
                httpStatus,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(globalException, httpStatus);
    }

    @ExceptionHandler(value = {RedisConnectionException.class})
    public ResponseEntity<Object> redisConnectionException(RedisConnectionException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        GlobalException globalException = new GlobalException(
                e.getMessage(),
                httpStatus,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(globalException, httpStatus);
    }

    @ExceptionHandler(value = {SaveToDbException.class})
    public ResponseEntity<Object> saveToDbException(SaveToDbException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        GlobalException globalException = new GlobalException(
                e.getMessage(),
                httpStatus,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(globalException, httpStatus);
    }
}
