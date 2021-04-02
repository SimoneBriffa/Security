package com.supportportal.Security.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.supportportal.Security.constant.SecurityConstant;
import com.supportportal.Security.domain.UserPrincipal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Component
public class JWTTokenProvider {
	
	//@Value("${jwt.secret}")
	private String secret = "[a-zA-Z-9._]^+$Guidelines89797987forAlphabeticalArraNumeralsandOtherSymbol$";
	
	
	//PRINCIPALE
	public String generateJwtToken(UserPrincipal userPrincipal) {
		
		String[] claims = getClaimsFromUser(userPrincipal);
		//claim = richiesta
		
		return JWT.create().withIssuer(SecurityConstant.GET_ARRAYS_LLC) //issuer = emittente
				.withAudience(SecurityConstant.GET_ARRAYS_ADMINISTRATION)
				.withIssuedAt(new Date()).withSubject(userPrincipal.getUsername()) //momento emissione e utente che manda il token
				.withArrayClaim(SecurityConstant.AUTHORITIES, claims) //come parametri abbiamo nome e valore
				.withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstant.EXPIRATION_TIME))
				.sign(Algorithm.HMAC512(secret.getBytes())); //algoritmo di criptazione
		
	}

	//PRINCIPALE
	public List<GrantedAuthority> getAuthorities(String token){
		
		String[] claims = getClaimsFromToken(token);
		
		return stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList()); 
		//conversione da array a list; la struttura dell'algoritmo è simile ad un foreach;
		//stream gioca il ruolo dell'iteratore, map si occupa di convertire ciascuno degli oggetti
	}
	
	//PRINCIPALE
	public Authentication getAuthentication(String username, List<GrantedAuthority> authorities,
											HttpServletRequest request) {
		
		//La classe Authentication rappresenta un token
		
		UsernamePasswordAuthenticationToken userPasswordAuthTok = new UsernamePasswordAuthenticationToken(username, 
															null, authorities);
		
		userPasswordAuthTok.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		
		return userPasswordAuthTok;
	}
	
	public boolean isTokenValid(String username, String token) {
		JWTVerifier verifier = getJWTVerifier();
		return StringUtils.isNotEmpty(username) && !isTokenExpired(verifier, token);
	}
	
	public boolean isTokenExpired(JWTVerifier verifier, String token) {
		Date expiration = verifier.verify(token).getExpiresAt();
		return expiration.before(new Date(System.currentTimeMillis() - SecurityConstant.EXPIRATION_TIME));
	}
	
	public String getSubject(String token) {
		JWTVerifier verifier = getJWTVerifier();
		return verifier.verify(token).getSubject();
	}
	
	
	private String[] getClaimsFromToken(String token) {
		
		JWTVerifier verifier = getJWTVerifier();
		return verifier.verify(token).getClaim(SecurityConstant.AUTHORITIES).asArray(String.class);
		
	}
	
	private JWTVerifier getJWTVerifier() {
		JWTVerifier verifier;
		try {
			Algorithm algorithm = Algorithm.HMAC512(secret);
			verifier = JWT.require(algorithm).withIssuer(SecurityConstant.GET_ARRAYS_LLC).build();
		} catch(JWTVerificationException e) {
			throw new JWTVerificationException(SecurityConstant.TOKEN_CANNOT_BE_VERIFIED);
		}
		
		return verifier;
	}
	
	private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
		//granted = concesso
		List<String> authorities = new ArrayList<>();
		
		for(GrantedAuthority grandAuth: userPrincipal.getAuthorities())
			authorities.add(grandAuth.getAuthority());
		
		return authorities.toArray(new String[0]);
		
	}

}
