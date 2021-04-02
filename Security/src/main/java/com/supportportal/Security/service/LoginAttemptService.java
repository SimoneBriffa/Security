package com.supportportal.Security.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
	
	private static final int MAX_NUM_OF_ATTEMPTS = 3; //massimo numero di tentativi prima del blocco account
	
	private static final int ATTEMPT_INCREMENT = 1;
		
	private LoadingCache<String, Integer> loginAttemptCache;
	
	@Autowired
	public LoginAttemptService() {
		super();
		this.loginAttemptCache = CacheBuilder.newBuilder()
				.expireAfterWrite(15, TimeUnit.MINUTES).maximumSize(100)
				.build(new CacheLoader<String, Integer>(){
					public Integer load(String key) {
						return 0;
					}
				});
	}

	//Rimuovi dalla cache dei tentativi
	public void evictUserFromLoginAttemptCache(String username) {
		loginAttemptCache.invalidate(username);
	}
	
	//Aggiungi alla cache dei tentativi
	public void addUserToLoginAttempCache(String username) {
		int attempts = 0;
		try {
			attempts = ATTEMPT_INCREMENT + loginAttemptCache.get(username);
			
			/*La struttura è quella di una mappa, dunque applicando .get(username)
			ci ritorna il value che è Integer, che rappresenta il numero di tentativi
			di accesso */
			
			loginAttemptCache.put(username, attempts);
		}catch(ExecutionException e) {
			e.printStackTrace();			
		}
		
	}
		
		public boolean hasExceededMaxAttempts(String username) throws ExecutionException {
			return loginAttemptCache.get(username) >= MAX_NUM_OF_ATTEMPTS;
		}
	}
	
	

