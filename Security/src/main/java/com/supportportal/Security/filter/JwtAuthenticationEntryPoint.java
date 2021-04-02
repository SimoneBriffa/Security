package com.supportportal.Security.filter;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportportal.Security.constant.SecurityConstant;
import com.supportportal.Security.domain.HttpResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint{

	//Questa è la classe che si occupa della protezione in caso di mancato accesso
	
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, 
					AuthenticationException exception) throws IOException {
		
		HttpResponse httpResponse = new HttpResponse(HttpStatus.FORBIDDEN.value(),
				HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase().toUpperCase(),
				SecurityConstant.FORBIDDEN_MESSAGE);
		//Tre argomenti: valore (403), motivazione e breve messaggio customizzato
		
		response.setContentType(APPLICATION_JSON_VALUE);
		response.setStatus(HttpStatus.FORBIDDEN.value());
		
		OutputStream outputStream = response.getOutputStream();
		
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(outputStream, httpResponse);
		outputStream.flush();
		
		//ObjectMapper ci serve a restituire in JSON i dati che descrivono il divieto di accesso
		
	}
	
	

}
