package com.supportportal.Security.domain;


import java.util.Collection;
import java.util.stream.Collectors;
import static java.util.Arrays.stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails{
	
	
	private static final long serialVersionUID = -2924050023279350930L;
	private User user;
	
	public UserPrincipal(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		//Granted Authority = Autorizzazione concessa, quindi stiamo convertendo le
		//stringhe di autorizzazione in SimpleGrantedAuthority, che estendono GrantedAuthirity
		
		return stream(this.user.getAuthorities()).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

	@Override
	public String getPassword() {		
		return this.user.getPassword();
	}

	@Override
	public String getUsername() {
				return this.user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {		
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {		
		return this.user.isNotLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {		
		return true;
	}

	@Override
	public boolean isEnabled() {
		return this.user.isActive();
	}
	
	

}