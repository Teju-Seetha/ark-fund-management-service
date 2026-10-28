package com.ark.fundmgmt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Resource Not Found");
        pd.setType(URI.create("https://ark.com/errors/not-found"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle("Insufficient Fund Balance");
        pd.setType(URI.create("https://ark.com/errors/insufficient-funds"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(CurrencyMismatchException.class)
    public ProblemDetail handleCurrencyMismatch(CurrencyMismatchException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Currency Mismatch");
        pd.setType(URI.create("https://ark.com/errors/currency-mismatch"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(DuplicateTransactionException.class)
    public ProblemDetail handleDuplicate(DuplicateTransactionException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Duplicate Transaction Detected");
        pd.setType(URI.create("https://ark.com/errors/idempotency-conflict"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
        pd.setTitle("Validation Failure");
        pd.setType(URI.create("https://ark.com/errors/validation-failure"));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred");
        pd.setTitle("Internal Server Error");
        pd.setProperty("error", ex.getMessage());
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
