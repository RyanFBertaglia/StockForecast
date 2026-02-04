package com.learn.exception;

public class ModelNotLoaded extends RuntimeException {
    public ModelNotLoaded(String message) {
        super(message);
    }
}
