package com.supportportal.Security.exception.domain;

public class EmailExistsException extends Exception{
	
	private static final long serialVersionUID = 1708373730837717733L;

	public EmailExistsException(String message) {
		super(message);
	}

}
