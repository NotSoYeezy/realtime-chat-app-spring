package com.chat.realtimechat.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Map<String, Integer> FIELD_PRIORITY = Map.of(
            "username", 3,
            "email", 2,
            "password", 1
    );

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.name(),
                message,
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        var err = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .max(Comparator.comparingInt(
                        e -> FIELD_PRIORITY.getOrDefault(e.getField(), 0)
                ))
                .orElse(null);

        String msg = (err != null) ? err.getDefaultMessage() : "Validation error";

        return buildError(HttpStatus.BAD_REQUEST, msg, request);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserExists(
            UserAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request
    ){
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler({
            LoginUserNotFoundException.class,
            IncorrectPasswordException.class
    })
    public ResponseEntity<ApiError> handleAuthErrors(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, "Invalid username or password", request);
    }

    @ExceptionHandler({
            RefreshTokenExpiredException.class,
            IncorrectRefreshTokenException.class
    })
    public ResponseEntity<ApiError> handleRefreshTokenExpired(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

}




