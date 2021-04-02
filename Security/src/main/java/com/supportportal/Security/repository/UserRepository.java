package com.supportportal.Security.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supportportal.Security.domain.User;

public interface UserRepository extends JpaRepository<User, Long>{
	
	User findByUsername(String username); 
	//Questo si traduce in SELECT * FROM user WHERE username = 'username'
	
	User findByEmail(String email);

	//@Query("")
	List<User> findAll();

}
