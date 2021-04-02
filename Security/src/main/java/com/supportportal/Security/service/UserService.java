package com.supportportal.Security.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.supportportal.Security.domain.User;
import com.supportportal.Security.exception.domain.EmailExistsException;
import com.supportportal.Security.exception.domain.EmailNotFoundException;
import com.supportportal.Security.exception.domain.UserNotFoundException;
import com.supportportal.Security.exception.domain.UsernameExistsException;

public interface UserService {
	
	User register(String firstName, String lastName, String username, String email) 
			throws UserNotFoundException, UsernameExistsException, EmailExistsException;
	
	List<User> getUsers();
	
	User findUserByUsername(String username);
	
	User findUserByEmail(String email);
	
	User addNewUser(String firstName, String lastName, String username,
			String email, String role, boolean isNotLocked, boolean isAcrive,
			MultipartFile profileImage) throws UsernameExistsException, EmailExistsException; //aggiunta user lato admin
	
	User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername,
			String newEmail, String newRole, boolean isNotLocked, boolean isAcrive,
			MultipartFile profileImage) 
					throws UserNotFoundException, UsernameExistsException, EmailExistsException;
	
	void deleteUser(Long userId);
	
	void resetPassword(String email) throws EmailNotFoundException;
	
	User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException;
	
}
