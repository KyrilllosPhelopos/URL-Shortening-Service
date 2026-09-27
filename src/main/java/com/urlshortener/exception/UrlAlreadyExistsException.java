package com.urlshortener.exception;

public class UrlAlreadyExistsException extends RuntimeException {
	
	public UrlAlreadyExistsException(String message) {
        super(message);
    }

}
