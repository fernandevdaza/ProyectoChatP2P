package com.fernandev.chatp2p.controller.exception;

public class UnreachableException extends RuntimeException {
    public UnreachableException(String message) {
        super(message);
    }
}
