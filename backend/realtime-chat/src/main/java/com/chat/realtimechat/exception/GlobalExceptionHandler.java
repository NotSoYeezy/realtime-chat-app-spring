package com.chat.realtimechat.exception;

import com.chat.realtimechat.exception.friendship.*;
import com.chat.realtimechat.exception.google.AccountNotConnectedException;
import com.chat.realtimechat.exception.google.ConnectionExpiredException;
import com.chat.realtimechat.exception.google.NoAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;

@Slf4j
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

    @ExceptionHandler({EmailAlreadyExistsException.class, UserNotConfirmedException.class})
    public ResponseEntity<ApiError> handleEmailExists(
            Exception ex,
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
        return buildError(HttpStatus.BAD_REQUEST, "Invalid login or password", request);
    }

    @ExceptionHandler({
            TokenExpiredException.class
    })
    public ResponseEntity<ApiError> handleTokenExpired(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler({TokenNotFoundException.class, UserNotFoundException.class, GroupNotFoundException.class})
    public ResponseEntity<ApiError> handleResourceNotFound(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(CannotFriendYourselfException.class)
    public ResponseEntity<ApiError> handleSelfFriend(
            CannotFriendYourselfException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, "You cannot send a friend request to yourself", request);
    }

    @ExceptionHandler(FriendshipAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleAlreadyFriends(
            FriendshipAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, "You are already friends", request);
    }

    @ExceptionHandler(PendingFriendRequestException.class)
    public ResponseEntity<ApiError> handlePendingRequest(
            PendingFriendRequestException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, "A friend request is already pending", request);
    }

    @ExceptionHandler(BlockedRelationshipException.class)
    public ResponseEntity<ApiError> handleBlocked(
            BlockedRelationshipException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.FORBIDDEN, "You cannot interact with this user", request);
    }

    @ExceptionHandler(FriendshipOperationException.class)
    public ResponseEntity<ApiError> handleFriendshipOperation(
            FriendshipOperationException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(GroupOperationException.class)
    public ResponseEntity<ApiError> handleGroupOperation(
            GroupOperationException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(GroupAccessDeniedException.class)
    public ResponseEntity<ApiError> handleGroupAccessDenied(
            GroupAccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiError> handleFileStorage(
            FileStorageException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(ex.getMessage(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }


    @ExceptionHandler(NoAuthenticationException.class)
    public ResponseEntity<ApiError> handleNoAuthentication(
            NoAuthenticationException ex,
            HttpServletRequest request){
        return buildError(HttpStatus.NOT_ACCEPTABLE, ex.getMessage(), request);
    }


    @ExceptionHandler(ConnectionExpiredException.class)
    public ResponseEntity<ApiError> handleConnectionExpired(
            ConnectionExpiredException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }


    @ExceptionHandler(AccountNotConnectedException.class)
    public ResponseEntity<ApiError> handleAccountNotConnected(
            AccountNotConnectedException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

}




