package com.ecomerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecomerce.model.Product;
import com.ecomerce.service.CartService;
import com.ecomerce.service.ProductService;
import com.ecomerce.service.UserService;
import com.ecomerce.util.FileUploadUtil;

import jakarta.servlet.http.HttpSession;

import com.ecomerce.model.User;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
	
	 @Autowired
	    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;
    
   
       //purchase product or buy button
    @GetMapping("buy/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "products/productDetails";
    }

    @GetMapping
    public String listProducts1(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products/list";
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "products/form";
    }

    @PostMapping
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam("imageFile") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); 
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File uploadFile = new File(uploadDir + fileName);
            
            file.transferTo(uploadFile);
            product.setProductImage(fileName);
        } else {
            product.setProductImage(null);
        }

        productService.saveProduct(product);
        return "redirect:/products";
    }

    
    //update controller
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product != null) {
            model.addAttribute("product", product); 
            return "products/edit-product";
        }
        return "redirect:/products"; 
    }
//update controller after submit
    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable("id") Long id,
                              @ModelAttribute("product") @Validated Product product,
                              BindingResult result,
                              @RequestParam("imageFile") MultipartFile file,
                              Model model,
                              RedirectAttributes redirectAttributes) throws IOException {

        if (result.hasErrors()) {
            return "products/edit-product";
        }

        Product existingProduct = productService.getProductById(id);

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

        product.setId(id);
        productService.updateProduct(id, product, file);
        redirectAttributes.addFlashAttribute("message", "Product updated successfully!");

        return "redirect:/products";
    }

    //delete controller
    @PostMapping("delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        
        redirectAttributes.addFlashAttribute("message", "Product deleted successfully!");
        
        return "redirect:/products";
    }
    
    
    
    
    @GetMapping("/index")
    public String listProducts2(HttpSession session,Model model) {
        List<Product> products = productService.getAllProducts();
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("loggedInUser", loggedInUser);

        if (loggedInUser == null) {
            // Redirect to login page if not logged in
            return "redirect:/users/login";
        }
        int cartItemCount = cartService.getCartItemCount(loggedInUser != null ? loggedInUser.getId() : null);
        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("products", products);
        String successMessage = (String) model.asMap().get("successMessage");
        String errorMessage = (String) model.asMap().get("errorMessage");
        model.addAttribute("successMessage", successMessage);
        model.addAttribute("errorMessage", errorMessage);
        return "products/index";
    }

    //user redirection mapping
//admin
//@GetMapping("/admin")
//private String Admin() {
//	return "/index/producer-dashboard";
//}


	//mainpage redirect
@GetMapping("/main")
private String mainPage() {
	return "/index/mainpage";
}
//about page
@GetMapping("/aboutpage")
public String about() {
	return "/comman/about";
}






}



