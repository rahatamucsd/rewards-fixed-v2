package com.rewards.exception;

import com.rewards.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("404 No handler: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return new ErrorResponse(404, "No endpoint: " + ex.getHttpMethod() + " " + ex.getRequestURL(), request.getRequestURI(), null);
    }
    @ExceptionHandler(CustomerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleCustomerNotFound(CustomerNotFoundException ex, HttpServletRequest request) {
        log.warn("404 {}", ex.getMessage());
        return new ErrorResponse(404, ex.getMessage(), request.getRequestURI(), null);
    }
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHandlerMethodValidation(HandlerMethodValidationException ex, HttpServletRequest request) {
        List<String> details = ex.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(err -> result.getMethodParameter().getParameterName()
                                + ": " + err.getDefaultMessage()))
                .collect(Collectors.toList());
        log.warn("400 Validation failed on {}: {}", request.getRequestURI(), details);
        return new ErrorResponse(400, "Validation failed", request.getRequestURI(), details);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.toList());
        log.warn("400 Constraint violation on {}: {}", request.getRequestURI(), details);
        return new ErrorResponse(400, "Validation failed", request.getRequestURI(), details);
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String detail = String.format("Required parameter '%s' of type %s is missing",
                ex.getParameterName(), ex.getParameterType());
        log.warn("400 Missing parameter on {}: {}", request.getRequestURI(), detail);
        return new ErrorResponse(400, detail, request.getRequestURI(), null);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String msg = String.format("Parameter '%s' must be of type %s",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("400 Type mismatch on {}: {}", request.getRequestURI(), msg);
        return new ErrorResponse(400, msg, request.getRequestURI(), null);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("400 Illegal argument on {}: {}", request.getRequestURI(), ex.getMessage());
        return new ErrorResponse(400, ex.getMessage(), request.getRequestURI(), null);
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("500 Unhandled exception on {}", request.getRequestURI(), ex);
        return new ErrorResponse(500, "An unexpected error occurred.", request.getRequestURI(), null);
    }
}