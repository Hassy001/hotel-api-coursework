package com.hotel.exception;

public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String resourceType, Object id) {
        super(resourceType + " with id '" + id + "' does not exist.");
    }
}
