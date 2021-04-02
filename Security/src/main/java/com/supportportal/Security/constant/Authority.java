package com.supportportal.Security.constant;

public class Authority {
	
	public static final String[] USER_AUTHORITIES = {"user:read"};
	public static final String[] HR_AUTHORITIES = {"user:read", "user:update" };
	//significa poter leggere e aggiornare istanze di user
	public static final String[] MANAGER_AUTHORITIES = { "user:read", "user:update"};
	public static final String[] ADMIN_AUTHORITIES = {"user:read", "user:update", "user:create"};
	public static final String[] SUPER_ADMIN_AUTHORITIES = {"user:read", "user:update", "user:delete",
														 "user:create"};
		
}
