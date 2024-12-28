package com.ecomerce.controller;

import org.hibernate.boot.jaxb.mapping.DiscriminatedAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecomerce.model.Product;
import com.ecomerce.model.User;
import com.ecomerce.repository.UserRepository;
import com.ecomerce.service.ProductService;
import com.ecomerce.service.UserService;

import jakarta.annotation.Generated;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private UserService userService;
    private UserRepository userRepository;
    
    @Autowired
    private ProductService productService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "Users/user-list";
    }
    
//    @GetMapping("/view/{id}")
//    public String viewUser(@PathVariable Long id, Model model) {
//        User user = userService.getUserById(id);
//        model.addAttribute("user", user);
//        return "Users/user-view"; 
//    }


    private String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) return null;

        String uniqueFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + uniqueFileName);
        Files.createDirectories(path.getParent()); 
        Files.write(path, file.getBytes());
        return uniqueFileName; 
    }

    @GetMapping("/new")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("isEdit", false); 
        return "Users/user-form";
    }

    @PostMapping
    public String saveUser(@ModelAttribute User user,   @RequestParam String useremail,
                @RequestParam("file") MultipartFile file, Model model) {
        if (!user.getPassword().equals(user.getConfirmpassword())) {
            model.addAttribute("error", "Password and Confirm Password do not match!");
            model.addAttribute("isEdit", false);
            return "Users/user-form";
        }
        if (userService.isEmailExist(useremail)) {
            model.addAttribute("error", "Email is already registered. Please use a different email.");
            return "Users/user-form"; 
        }

        try {
            if (!file.isEmpty()) {
                user.setProfileimage(saveFile(file)); 
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error occurred while saving the file.");
            return "Users/user-form";
        }

        userService.saveUser(user);
        return "redirect:users/login";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User existingUser = userService.getUserById(id);
        existingUser.setPassword(null); 
        existingUser.setConfirmpassword(null); 
        model.addAttribute("user", existingUser);
        model.addAttribute("isEdit", true);
        return "Users/edit-form";
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute User user,
                             @RequestParam(value = "file", required = false) MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        try {
            User existingUser = userService.getUserById(user.getId());

            user.setPassword(existingUser.getPassword());
            user.setConfirmpassword(existingUser.getConfirmpassword());

            if (file != null && !file.isEmpty()) {
                String fileName = saveFile(file);
                user.setProfileimage(fileName);   
            } else {
                user.setProfileimage(existingUser.getProfileimage());
            }

            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
        redirectAttributes.addFlashAttribute("successMessage", "User details updated successfully!");

        return "redirect:/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }
    
    //login logic
    //login page logic here
    
    @GetMapping("/login")
    public String showLoginPage() {
        return "Users/login";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam String identifier, 
            @RequestParam String password,
            @RequestParam String role,
            HttpSession session,
            Model model) {

        System.out.println("Login attempt with identifier: " + identifier + ", password: " + password + ", role: " + role);

        User user = userService.authenticateUser (identifier, password, role);
        
        if (user == null) {
            model.addAttribute("error", "Invalid email, firstname, password, or role!");
            System.out.println("Invalid login credentials");
            return "Users/login"; 
        }

        System.out.println("User  authenticated successfully. Role: " + user.getRole());
        session.setAttribute("loggedInUser", user);
        switch (user.getRole().toLowerCase()) {
            case "admin":
////                System.out.println("Redirecting to admin dashboard");
//                if (user == null || user.getId() == null) {
//                    System.out.println("User or User ID is null.");
//                    return "error";
//                }

                List<Product> product = productService.getproductbyUser(user.getId());

//                if (product == null || product.isEmpty()) {
//                    System.out.println("No products found for user ID: " + user.getId());
//                } else {
//                    System.out.println("Products retrieved: " + product);
//                }

                model.addAttribute("product", product);

//            model.addAttribute("product", product);
                return "redirect:/users/dash"; 
            case "user":
                System.out.println("Redirecting to user dashboard");
                return "redirect:/products/index"; 
            default:
                model.addAttribute("error", "Invalid role!");
                System.out.println("Invalid role");
                return "/users/login"; 
        }
        //redirect login logic
        
    }
    //redirect logic after login
  //redirect login producer to dashboard
    @GetMapping("/dash")
    public String listProducts1(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/users/login";
        }        
        System.out.print(loggedInUser);
        List<Product> products = productService.getproductbyUser(loggedInUser.getId());
        System.out.println("Products fetched for user " + loggedInUser.getId() + ": " + products);

        model.addAttribute("products", products);
        model.addAttribute("loggedInUser", loggedInUser);
        return "index/producer-dashboard";
    }




    
    //logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/users/login";
    }
    
    
    //reset password
 //password foregot
    @GetMapping("/forgot")
    public String forgot() {
        return "/Users/forgot-password";
    }


    @PostMapping("/request-reset")
    public String requestPasswordReset(@RequestParam String useremail, Model model) {
        try {
            boolean success = userService.generateResetToken(useremail);
            if (success) {
                String resetToken = userService.getResetTokenForEmail(useremail);
                String resetLink = "http://localhost:7070/users/reset?token=" + resetToken;

                model.addAttribute("message", "Reset link generated successfully! <a href=\"" + resetLink + "\">Click here</a> to reset your password.");
                model.addAttribute("alertClass", "alert-success");
            } else {
                model.addAttribute("message", "Could not find user with the provided email.");
                model.addAttribute("alertClass", "alert-danger");
            }
        } catch (Exception e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            model.addAttribute("alertClass", "alert-danger");
        }
        return "/Users/forgot-password";
    }


   
    @GetMapping("/reset")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        if (userService.validateResetToken(token)) {
            model.addAttribute("token", token); 
            return "/Users/reset-password-form"; 
        }
        return "/Users/invalid-token";
    }
   
    @PostMapping("/reset")
    public String resetPassword(@RequestParam("token") String token,
                                                @RequestParam("password") String password,
                                                RedirectAttributes redirectAttributes) {
        boolean success = userService.resetPassword(token, password);
        if (success) {
        	 redirectAttributes.addFlashAttribute("message", "Password updated successfully.");
             redirectAttributes.addFlashAttribute("alertClass", "alert-success");
             return "redirect:/users/login";             
        }
        redirectAttributes.addFlashAttribute("message", "Invalid or expired token.");
        redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
        return "redirect:/reset-password-form";    
        }

@GetMapping("/invalid")
public String invalid() {
	return "/Users/invalid-token";
}


//added new product
@GetMapping("/addproduct")
public String showAddProductForm(HttpSession session, Model model) {
    User loggedInUser = (User) session.getAttribute("loggedInUser");
    model.addAttribute("product", new Product());

    if (loggedInUser == null) {
        model.addAttribute("error", "Please login first.");
        return "redirect:/users/login";
    }

    return "index/add-product";
}

//adding product of login user name
@PostMapping("/addproduct")
public String saveProduct(@ModelAttribute("product") Product product,
                          @RequestParam("imageFile") MultipartFile file ,HttpSession session) throws IOException {
    if (!file.isEmpty()) {
    	String uploadDir = System.getProperty("user.dir") + "/uploads/";

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs(); 
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File uploadFile = new File(uploadDir + fileName);

        String contentType = file.getContentType();
        if (!contentType.startsWith("image/")) {
            throw new IOException("Only image files are allowed.");
        }

        file.transferTo(uploadFile);

        product.setProductImage(fileName);
    } else {
        product.setProductImage(null);
    }
    User loggedInUser = (User) session.getAttribute("loggedInUser"); 
    if (loggedInUser != null) {
        product.setUser(loggedInUser);  
    }

    productService.saveProduct(product);

    return "redirect:/users/dash";
}

//edit product of login user
@GetMapping("/{id}/editProduct")
public String showEditProductForm(@PathVariable Long id,HttpSession session, Model model) {
	Product product=productService.getProductById(id);
	if (product==null) {
		model.addAttribute("error","you have no product");
		return "redirect:/users/dash";
		
	}
    User loggedInUser = (User) session.getAttribute("loggedInUser");
    model.addAttribute("product", new Product());

    if (loggedInUser == null) {
        model.addAttribute("error", "Please login first.");
        return "redirect:/users/login";
    }
System.out.println(id);
model.addAttribute("product",product);

    return "index/user-editproduct";
}

@PostMapping("/{id}/editProduct")
public String updateProduct(@PathVariable("id") Long id,
                            @ModelAttribute("product") @Validated Product product,
                            BindingResult result,
                            @RequestParam("imageFile") MultipartFile file,
                            Model model,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) throws IOException {

    if (result.hasErrors()) {
        return "index/user-editproduct";
    }

    Product existingProduct = productService.getProductById(id);
    if (existingProduct == null) {
        redirectAttributes.addFlashAttribute("error", "Product not found.");
        return "redirect:/users/dash";
    }

    if (!file.isEmpty()) {
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File uploadFile = new File(uploadDir + fileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, uploadFile.toPath());
        } catch (IOException e) {
            throw new IOException("Failed to save file: " + e.getMessage(), e);
        }

        product.setProductImage(fileName);
    } else {
        product.setProductImage(existingProduct.getProductImage());
    }

    User loggedInUser = (User) session.getAttribute("loggedInUser");
    if (loggedInUser == null) {
        redirectAttributes.addFlashAttribute("error", "Please login first.");
        return "redirect:/users/login";
    }

    product.setId(id);
    product.setUser(loggedInUser);
    productService.updateProduct(id, product, file);

    redirectAttributes.addFlashAttribute("message", "Product updated successfully!");

    return "redirect:/users/dash";
}

//delete product of logdinser
@PostMapping("productdelete/{id}")
public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes,HttpSession session) {
	 User loggedInUser = (User) session.getAttribute("loggedInUser");
	    if (loggedInUser == null) {
	        redirectAttributes.addFlashAttribute("error", "Please login first.");
	        return "redirect:/users/login";
	    }
    productService.deleteProduct(id);
    
    redirectAttributes.addFlashAttribute("message", "Product deleted successfully!");
    
    return "redirect:/users/dash";
}
//view profile of logdinuser





//@GetMapping("/view2/{id}")
//public String viewUser(@PathVariable Long id, Model model) {
//    User user = userService.getUserById(id);
//    if (user != null) {
//        model.addAttribute("user", user);
//        return "index/loginuser-view";
//    } else {
//        return "redirect:/users/login";
//    }
//}
@GetMapping("/view2/{id}")
public String viewUser2(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    // Fetch the logged-in user from the session
    User loggedInUser = (User) session.getAttribute("loggedInUser");
    
    // Check if the user is logged in
    if (loggedInUser == null) {
        redirectAttributes.addFlashAttribute("error", "Please log in to view the profile.");
        return "redirect:/users/login"; // Redirect to login if user is not logged in
    }

    // Fetch the requested user's details from the database
    User user = userService.getUserById(id);
    if (user == null) {
        redirectAttributes.addFlashAttribute("error", "User not found.");
        return "redirect:/users/dash"; // Redirect to the dashboard if user is not found
    }

    // Add the user details to the model for display
    model.addAttribute("user", user);

    return "index/loginuser-view"; // Return the view template
}







}
