package com.supportportal.Security.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import com.supportportal.Security.service.LoginAttemptService;

@Component
public class AuthenticationFailureListener {
	
	private LoginAttemptService loginAttempService;

	@Autowired
	public AuthenticationFailureListener(LoginAttemptService loginAttempService) {
		super();
		this.loginAttempService = loginAttempService;
	}
	
	@EventListener
	public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
		Object principal = event.getAuthentication().getPrincipal();
		if(principal instanceof String) {
			String username = (String) event.getAuthentication().getPrincipal();
			loginAttempService.addUserToLoginAttempCache(username);
		}
	}

}
