package com.ecomerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;

import com.ecomerce.model.User;
import com.ecomerce.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public void updateUser(User user) {
        try {
            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + user.getId()));

            existingUser.setFirstname(user.getFirstname());
            existingUser.setLastname(user.getLastname());
            existingUser.setUseremail(user.getUseremail());
            existingUser.setAddress(user.getAddress());
            existingUser.setCountry(user.getCountry());
            existingUser.setState(user.getState());
            existingUser.setCity(user.getCity());
            existingUser.setPincode(user.getPincode());
            existingUser.setRole(user.getRole());


            if (user.getProfileimage() != null && !user.getProfileimage().isEmpty()) {
                existingUser.setProfileimage(user.getProfileimage());
            }

            userRepository.save(existingUser);
        } catch (Exception e) {
            throw new RuntimeException("Error while updating user: " + e.getMessage(), e);
        }
    }
    
    //login
    public User authenticateUser(String identifier, String password, String role) {
        System.out.println("Attempting authentication with identifier: " + identifier + ", password: " + password + ", role: " + role);
        User user = userRepository.findByFirstnameAndPasswordAndRole(identifier, password, role);
        System.out.println("Query result: " + user); 
        return user;
    }

    
    public boolean isEmailExist(String useremail) {
        return userRepository.existsByUseremail(useremail);
    }
    
    //for password
    
    @Autowired
    private JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    
    public boolean generateResetToken(String useremail) {
        try {
            Optional<User> userOpt = userRepository.findByUseremail(useremail);
            
            if (!userOpt.isPresent()) {
                logger.error("User not found for email: {}", useremail);
                return false;
            }
            
            User user = userOpt.get();
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            
            userRepository.save(user);
            
            logger.info("Reset token generated and saved for user: {}", useremail);
            return true;
        } catch (Exception e) {
            logger.error("Error generating reset token for user: {}", useremail, e);
            return false;
        }
    }
    public String getResetTokenForEmail(String useremail) {
        Optional<User> userOpt = userRepository.findByUseremail(useremail);
        if (userOpt.isPresent()) {
            return userOpt.get().getResetToken();
        }
        return null;
    }


   
    public boolean validateResetToken(String token) {
        return userRepository.findByResetToken(token).isPresent();
    }

    public boolean resetPassword(String token, String password) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(password);
            user.setResetToken(null); 
            userRepository.save(user);
            return true;
        }
        return false; 
    }

   
  //user added product
    public User getUserByuseremail(String useremail) {
    	return userRepository.findByUseremail(useremail).orElseThrow(()->new RuntimeException("Producer not found"));
    	
    }
    
    //get user firstname in buy page
  
    public User findByFirstname(String firstname) {
        return userRepository.findByFirstname(firstname);
    }
    

}