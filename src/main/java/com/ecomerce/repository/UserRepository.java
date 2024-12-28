 	package com.ecomerce.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomerce.model.User;


public interface UserRepository extends JpaRepository<User, Long> {
	User findByUseremailAndPasswordAndRole(String useremail, String password, String role);
    User findByFirstnameAndPasswordAndRole(String firstname, String password, String role);
    boolean existsByUseremail(String useremail);
    
    //forgot password
    Optional<User> findByUseremail(String useremail); 
    Optional<User> findByResetToken(String resetToken);
    
    // Find a user by firstname
    User findByFirstname(String firstname);
}
