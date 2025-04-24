package com.skiply.fee.exception;

public class InvalidFeeAmountException extends RuntimeException {
    public InvalidFeeAmountException(String message) {
        super(message);
    }
}