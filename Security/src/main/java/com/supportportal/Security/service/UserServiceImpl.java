package com.supportportal.Security.service;

import com.supportportal.Security.constant.FileConstant;
import com.supportportal.Security.domain.User;
import com.supportportal.Security.domain.UserPrincipal;
import com.supportportal.Security.enumeration.Role;
import com.supportportal.Security.exception.domain.EmailExistsException;
import com.supportportal.Security.exception.domain.EmailNotFoundException;
import com.supportportal.Security.exception.domain.UserNotFoundException;
import com.supportportal.Security.exception.domain.UsernameExistsException;
import com.supportportal.Security.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService{
	
	private static final String NO_USER_FOUND_BY_USERNAME = "No user found by username ";
	private static final String NO_USER_FOUND_BY_EMAIL = "No user found by email ";
	private static final String USERNAME_ALREADY_EXISTS = "Username already exists";
	private static final String EMAIL_ALREADY_EXISTS = "Email already exists";
	private Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	User user = new User();
	
	private UserRepository userRepository;
	private BCryptPasswordEncoder passwordEncoder;
	private LoginAttemptService loginAttemptService;
	private EmailService emailService;
	
	@Autowired
	public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
			LoginAttemptService loginAttemptService, EmailService emailService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.loginAttemptService = loginAttemptService;
		this.emailService = emailService;
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user = userRepository.findByUsername(username);
		
		if(user == null) {
			LOGGER.error("User not found by username \"" + username + "\"");
			throw new UsernameNotFoundException("User not found by username \"" + username + "\"");
		}else {
			try {
				validateLoginAttempt(user);
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			user.setLastLoginDateDisplay(user.getLastLoginDate()); //preleviamo l'ultimo accesso
			user.setLastLoginDate(new Date()); //aggiorniamo l'ultimo accesso alla data corrente
			userRepository.save(user);
			UserPrincipal userPrincipal = new UserPrincipal(user);
			LOGGER.info("Returning found user by username \"" + username + "\"");
			return userPrincipal; //perch?? UserPrincipal implementa  UserDetails
		}
		
	}
	
	

	@Override
	public User register(String firstName, String lastName, String username, String email) 
			throws UserNotFoundException, UsernameExistsException, EmailExistsException{
		
		if(findUserByUsername(username) != null)
				throw new UsernameExistsException("This username already exists");

		if(findUserByEmail(email) != null)
				throw new EmailExistsException("This email already exists");
		
		user.setUsername(username);
		user.setEmail(email);
		user.setUserId(generatedUserId());
		String password = generatePassword();
		String encodedPassword = encodePassword(password);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setJoinDate(new Date());
		user.setPassword(encodedPassword);
		user.setActive(true);
		user.setNotLocked(true);
		user.setRole(Role.ROLE_USER.name());;
		user.setAuthorities(Role.ROLE_USER.getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
		
		userRepository.save(user);
		
		LOGGER.info("New user password: " + password);
		try {
			emailService.sendNewPasswordEmail(firstName, password, email);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
		return user;
		
	}
	

	@Override
	public List<User> getUsers() {
		return userRepository.findAll(Sort.by(Sort.Direction.ASC, "lastName"));
	}

	@Override
	public User findUserByUsername(String username) {	
		return userRepository.findByUsername(username);
	}

	@Override
	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public User addNewUser(String firstName, String lastName, String username, String email, String role,
			boolean isNotLocked, boolean isActive, MultipartFile profileImage) 
					throws UsernameExistsException, EmailExistsException {
		
		if(findUserByUsername(username) != null)
			throw new UsernameExistsException("This username already exists");

		if(findUserByEmail(email) != null)
			throw new EmailExistsException("This email already exists");
		
		User user = new User();
		String password = generatePassword();
		String encodedPassword = encodePassword(password);
		user.setUserId(generatedUserId());
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		user.setUsername(username);
		user.setJoinDate(new Date());
		user.setPassword(encodedPassword);
		user.setActive(isActive);
		user.setNotLocked(isNotLocked);
		user.setRole(Role.valueOf(role).name());
		user.setAuthorities(Role.valueOf(role).getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
		
		userRepository.save(user);
		try {
			saveProfileImage(user, profileImage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(user.getAuthorities());
		
		LOGGER.info("New user password: " + password);
		try {
			emailService.sendNewPasswordEmail(firstName, password, email);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
		return user;
	}

	@Override
	public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername,
			String newEmail, String newRole, boolean isNotLocked, boolean isActive, MultipartFile profileImage) 
					throws UserNotFoundException, UsernameExistsException, EmailExistsException {
		
			User currentUser = validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);
			
			currentUser.setFirstName(newFirstName);
			currentUser.setLastName(newLastName);
			currentUser.setUsername(newUsername);
			currentUser.setActive(isActive);
			currentUser.setEmail(newEmail);
			currentUser.setNotLocked(isNotLocked);
			currentUser.setRole(Role.valueOf(newRole).name());
			currentUser.setAuthorities(Role.valueOf(newRole).getAuthorities());
			try {
				saveProfileImage(currentUser, profileImage);
			} catch (IOException e){
				e.printStackTrace();
			}
			
			userRepository.save(currentUser);
			
			return currentUser;
			
	}


	@Override
	public void deleteUser(Long userId) {
		userRepository.deleteById(userId);
	}

	@Override
	public void resetPassword(String email) throws EmailNotFoundException {
		User user = userRepository.findByEmail(email);
		if(user == null)
			throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + email);
		
		String password = generatePassword();
		user.setPassword(encodePassword(password));
		userRepository.save(user);
		
		try {
			emailService.sendNewPasswordEmail(user.getFirstName(), password, email);
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException{
		
		User user = findUserByUsername(username);
		
		if(user == null)
			throw new UserNotFoundException("USER WITH USERNAME " + username + " DOES NOT EXISTS");
		
			try {
				saveProfileImage(user, profileImage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return user;
		} 
		
	
	
	//----------------------------------METODI DI SUPPPORTO
	
	private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) 
			 throws UserNotFoundException, UsernameExistsException, EmailExistsException {
		 
	        User userByNewUsername = findUserByUsername(newUsername);
	        User userByNewEmail = findUserByEmail(newEmail);
	        User currentUser = findUserByUsername(currentUsername);
	        
	        System.out.println("Validate " + currentUsername);
	        System.out.println("Validate " + currentUser);
	        
	            if(currentUser == null) {
	                throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME + currentUsername);
	            }
	            if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
	                throw new UsernameExistsException(USERNAME_ALREADY_EXISTS);
	            }
	            if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
	                throw new EmailExistsException(EMAIL_ALREADY_EXISTS);
	            }
	            return currentUser;
	        }
	 

	private void validateLoginAttempt(User user) throws ExecutionException {
		if(user.isNotLocked()) { //se non ?? bloccato
			if(loginAttemptService.hasExceededMaxAttempts(user.getUsername())){
				//se ha superato il numero massimo di tentativi...
				user.setNotLocked(false); //bloccalo
			}else 
				user.setNotLocked(true);
	} else {
		loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
	}
		
	}
	

	private String getTemporaryProfileImageUrl(String username) {
		return ServletUriComponentsBuilder.fromCurrentContextPath()
				.path(FileConstant.DEFAULT_USER_IMAGE_PATH + username).toUriString();
		/*questo ritorna il nome del server in cui sta girando l'applicazione 
		 * concatenato a quanto specificato in .path()
		 */
	}

	private String encodePassword(String password) {
		
		return passwordEncoder.encode(password);
	}

	private String generatePassword() {
		
		return RandomStringUtils.randomAlphanumeric(10);
	}

	private String generatedUserId() {
		return RandomStringUtils.randomNumeric(10);
	}

	private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
			
		if(profileImage != null) {
			Path userFolder = Paths.get(FileConstant.USER_FOLDER + user.getUsername())
													.toAbsolutePath().normalize();
		
			if(!Files.exists(userFolder)) {
					Files.createDirectories(userFolder);			
			
				LOGGER.info(FileConstant.DIRECTORY_CREATED + userFolder);
			}
			Files.deleteIfExists(Paths.get(userFolder + user.getUsername() + FileConstant.JPG_EXTENSION));
			Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername() 
					+ FileConstant.JPG_EXTENSION), StandardCopyOption.REPLACE_EXISTING);
			user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
			userRepository.save(user);
			LOGGER.info(FileConstant.FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
		}
	}
	
	private String setProfileImageUrl(String username) {
		
		return ServletUriComponentsBuilder.fromCurrentContextPath()
				.path(FileConstant.USER_IMAGE_PATH + username + FileConstant.FORWARD_SLASH
						+ username + FileConstant.JPG_EXTENSION).toUriString();
	}
	
			
	}
        

        



