package com.supportportal.Security.resource;


import com.supportportal.Security.constant.FileConstant;
import com.supportportal.Security.constant.SecurityConstant;
import com.supportportal.Security.domain.HttpResponse;
import com.supportportal.Security.domain.User;
import com.supportportal.Security.domain.UserPrincipal;
import com.supportportal.Security.exception.domain.*;
import com.supportportal.Security.service.UserService;
import com.supportportal.Security.utility.JWTTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping(value = "/user")
public class UserResource extends ExceptionHandling{
	
	private UserService userService;
	private AuthenticationManager authenticationManager;
	private JWTTokenProvider jwtTokenProvider;
		
	@Autowired
	public UserResource(UserService userService, AuthenticationManager authenticationManager,
			JWTTokenProvider jwtTokenProvider) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@PostMapping("/login")
	public ResponseEntity<User> login(@RequestBody User user) {
		
		authenticate(user.getUsername(), user.getPassword());
		
		User loginUser = userService.findUserByUsername(user.getUsername());
		UserPrincipal userPrincipal =  new UserPrincipal(loginUser);
		
		HttpHeaders jwtHeader = getJwtHeader(userPrincipal);	
		
		return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);
	} 

	@PostMapping("/register")
	public ResponseEntity<User> register(@RequestBody User user) 
			throws UserNotFoundException, UsernameExistsException, EmailExistsException {
	
		User newUser = userService.register(user.getFirstName(), user.getLastName(), 
							user.getUsername(), user.getEmail());
		
			return new ResponseEntity<>(newUser, HttpStatus.OK);
		}
		
	
	@PostMapping("/add")
	public ResponseEntity<User> addNewUser(@RequestParam("firstName") String firstName,
			@RequestParam("lastName") String lastName, @RequestParam("username") String username,
			@RequestParam("email") String email, @RequestParam("role") String role,
			@RequestParam("isActive") String isActive, @RequestParam("isNotLocked") String isNotLocked,
			@RequestParam(value = "profileImage", required = false) MultipartFile profileImage) 
					throws UsernameExistsException, EmailExistsException{
		
		if(userService.findUserByUsername(username) != null)
			throw new UsernameExistsException("This username already exists");
		else if(userService.findUserByEmail(email) != null)
			throw new EmailExistsException("This email already exists");
		else {
		User newUser = userService.addNewUser(firstName, lastName, username, email, role, 
				Boolean.parseBoolean(isNotLocked), Boolean.parseBoolean(isActive), profileImage);
		
		return new ResponseEntity<>(newUser, HttpStatus.OK);
		}
		
	}
	
	@PostMapping("/update")
	public ResponseEntity<User> updateUser(@RequestParam("firstName") String firstName,
			@RequestParam("currentUsername") String currentUsername,
			@RequestParam("lastName") String lastName, @RequestParam("username") String username,
			@RequestParam("email") String email, @RequestParam("role") String role,
			@RequestParam("isActive") String isActive, @RequestParam("isNotLocked") String isNotLocked,
			@RequestParam(value = "profileImage", required = false) MultipartFile profileImage) 
					throws UsernameExistsException, EmailExistsException, UserNotFoundException{
		
		User updatedUser = userService.updateUser(currentUsername, firstName, lastName, username, email, role, 
				Boolean.parseBoolean(isNotLocked), Boolean.parseBoolean(isActive), profileImage);
		
		return new ResponseEntity<>(updatedUser, HttpStatus.OK);
		}
		
	
	@GetMapping("/find/{username}")
	public ResponseEntity<User> getUser(@PathVariable("username") String username) 
			throws UserNotFoundException{
		
		User user = userService.findUserByUsername(username);
		
		if(user == null)
			throw new UserNotFoundException("User with username " + username + " does not exists");
		else
			return new ResponseEntity<>(user, HttpStatus.OK);
	}
	
	@GetMapping("/list")
	public ResponseEntity<List<User>> getAllUsers(){
		return new ResponseEntity<>(userService.getUsers(), HttpStatus.OK);
	}
	
	@GetMapping("/resetPassword/{email}")
	public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) 
			throws EmailNotFoundException{
		
		userService.resetPassword(email);
		return response(HttpStatus.OK, "Email sent to " + email);
	}
	
	@DeleteMapping("delete/{id}")
	@PreAuthorize("hasAnyAuthority('user:delete')")
	public ResponseEntity<HttpResponse> deleteUser(@PathVariable("id") long id){
		userService.deleteUser(id);
		return response(HttpStatus.OK, "Deleted user with id " + id);
	}
	
	@PostMapping("/updateProfileImage")
	public ResponseEntity<User> updateProfileImage(@RequestParam("username") String username,
			@RequestParam(value = "profileImage", required = false) MultipartFile profileImage) 
					throws UserNotFoundException{
			
		System.out.println("Controller " + username);
		
		return new ResponseEntity<>(userService.updateProfileImage(username, profileImage), HttpStatus.OK);
		
		}
	
	@GetMapping(path = "/image/{username}/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
	//questa funzione ritorna un array di byte, per come il browser interpeta un'immagine
	public byte[] getProfileImage(@PathVariable("username") String username,
				@PathVariable("fileName") String fileName) throws IOException {
		
		return Files.readAllBytes(Paths.get(FileConstant.USER_FOLDER + username
				+ FileConstant.FORWARD_SLASH + fileName));
		
	}
	
	@GetMapping(path = ("/image/profile/{username}"), produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] getTempProfileImage(@PathVariable("username") String username) throws IOException {
		
		URL url = new URL(FileConstant.TEMP_PROFILE_IMAGE_BASE_URL + username);
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		try(InputStream inputStream = url.openStream()){
			
			int bytesRead;
			byte[] chunk = new byte[1024];
			
			while((bytesRead = inputStream.read(chunk)) > 0) {
				byteArrayOutputStream.write(chunk, 0, bytesRead);
				
	//Parameters:b - the data.off - the start offset in the data.len - the number of bytes to write
				 
			}
			
		}
		
		return byteArrayOutputStream.toByteArray();
		
	}
		
	


	//--------------------------------------------
	
	private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(SecurityConstant.JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));
		
		return headers;
	}

	private void authenticate(String username, String password) {
		
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		
	}

	private ResponseEntity<HttpResponse> response(HttpStatus status, String message) {

		return new ResponseEntity<>(new HttpResponse(status.value(), status,
				status.getReasonPhrase().toUpperCase(), message.toUpperCase()), status);
	}

}