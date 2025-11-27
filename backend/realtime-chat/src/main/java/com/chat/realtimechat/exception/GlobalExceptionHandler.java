package com.chat.realtimechat.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

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
        String msg = ex.getBindingResult().getFieldError().getDefaultMessage();
        return buildError(HttpStatus.BAD_REQUEST, msg, request);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserExists(
            UserAlreadyExistsException ex,
            HttpServletRequest request
    ) {
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

}




