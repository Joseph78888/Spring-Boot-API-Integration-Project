package com.example.jsonwrapper.exceptions;

import org.springframework.http.HttpStatusCode;

public class ApiException extends RuntimeException {

    public ApiException(String message, HttpStatusCode statusCode) {
        super(message);
    }
}
