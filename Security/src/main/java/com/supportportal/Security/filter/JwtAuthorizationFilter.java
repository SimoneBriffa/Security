package com.supportportal.Security.filter;

import com.supportportal.Security.constant.SecurityConstant;
import com.supportportal.Security.utility.JWTTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

//Questa classe serve a stoppare gli utenti che non hanno effettuato il login o hanno un token sbagliato

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {
	
	/*OncePerRequestFilter garantisce che all'interno di una Request può accadere ogni cosa una sola volta */
	
	private JWTTokenProvider jwtTokenProvider;
	
	public JwtAuthorizationFilter(JWTTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	/*
	A FilterChain is an object provided by the servlet container to the developer giving a view into the
	invocation chain of a filtered request for a resource. Filters use the FilterChain to invoke the next
	filter in the chain, or if the calling filter is the last filter in the chain, to invoke the resource
	at the end of the chain.
	 */
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if(request.getMethod().equalsIgnoreCase(SecurityConstant.OPTIONS_HTTP_METHOD)) {
			response.setStatus(HttpStatus.OK.value());
			/*
		Lo standard Cross-Origin Resource Sharing funziona aggiungendo nuovi header HTTP che consentono ai server
		di descrivere l'insieme di origini che sono autorizzate a leggere quelle informazioni tramite un web browser.
		In aggiunta, per i metodi di richiesta HTTP che possono causare effetti collaterali sui dati del server
		(in particolare, per i metodi HTTP diversi da GET, o per l'utilizzo di POST con determinati MIME types),
		la specifica prevede che il browser "anticipi" la richiesta (questa operazione è detta "preflight"),
		richiedendo al server i metodi supportati tramite una richiesta HTTP OPTIONS, e poi, dopo una "approvazione"
		del server, invii la richiesta effettiva con il metodo HTTP effettivo. I server possono anche informare i client
		se delle "credenziali" (inclusi Cookies e dati di autenticazione HTTP) debbano essere inviate insieme alle richieste.

		In parola povere è una richiesta che il client fa al server per "sapere" se il server accetti
		determinati tipi di richieste o di header. Dunque non c'è motivo di non accettare questo
		tipo di richiesta, la autorizziamo sempre.
		 */
		} else {
			String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
			if(authorizationHeader == null || !authorizationHeader.startsWith(SecurityConstant.TOKEN_PREFIX)) {
				//in questo caso il token è sbagliato
				filterChain.doFilter(request, response);
				return;
			}
			String token = authorizationHeader.substring(SecurityConstant.TOKEN_PREFIX.length());
			String username = jwtTokenProvider.getSubject(token);
			
			if(jwtTokenProvider.isTokenValid(username, token) && 
					SecurityContextHolder.getContext().getAuthentication() == null) {
			//Cioè se il token è valido ma l'autenticazione è null allora impostiamola "manualmente"
				List<GrantedAuthority> authorities = jwtTokenProvider.getAuthorities(token);
				Authentication authentication = jwtTokenProvider.getAuthentication(username, authorities, request);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}else {
				SecurityContextHolder.clearContext();
			}
			
		}
		
		filterChain.doFilter(request,  response);

	}

}
