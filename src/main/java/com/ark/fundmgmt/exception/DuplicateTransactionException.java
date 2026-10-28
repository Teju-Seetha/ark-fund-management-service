package com.ark.fundmgmt.exception;
public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String message) { super(message); }
}
