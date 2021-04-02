package com.supportportal.Security.exception.domain;

public class UserNotFoundException extends Exception{
	
	private static final long serialVersionUID = -1342618953909345177L;

	public UserNotFoundException(String message) {
		super(message);
		
	}
}
