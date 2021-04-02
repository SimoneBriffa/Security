package com.supportportal.Security.exception.domain;

public class UsernameExistsException extends Exception{
	
	
	private static final long serialVersionUID = 5448079369854404365L;

	public UsernameExistsException(String message) {
		super(message);
	}

}
