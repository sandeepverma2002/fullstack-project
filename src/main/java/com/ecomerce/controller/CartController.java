package com.ecomerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecomerce.model.Cart;
import com.ecomerce.model.Product;
import com.ecomerce.model.User;
import com.ecomerce.service.CartService;
import com.ecomerce.service.ProductService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    // View Cart
    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/users/login"; 
        }

        // Fetching cart items and total price
        List<Cart> cartItems = cartService.getCartItems(loggedInUser);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", cartService.calculateTotalPrice(loggedInUser));

        // Adding total quantity of products in the cart
        model.addAttribute("totalQuantity", cartService.getTotalProductQuantity(loggedInUser));

        return "products/cart-view";
    }

    // Add to Cart
    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        // Retrieve the logged-in user from the session
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/users/login"; 
        }

        // Retrieve the product by its ID
        Product product = productService.getProductById(productId);

        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Product not found.");
            return "redirect:/products/index";
        }

        cartService.addToCart(loggedInUser, product, quantity);

        redirectAttributes.addFlashAttribute("successMessage", "Product added to cart successfully!");

        // Add the total quantity of products in the cart
        int totalQuantity = cartService.getTotalProductQuantity(loggedInUser);
        redirectAttributes.addFlashAttribute("totalQuantity", totalQuantity);

        return "redirect:/products/index";
    }




    // Remove from Cart
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/users/login"; 
        }

        Product product = productService.getProductById(productId);
        cartService.removeFromCart(loggedInUser, product);

        redirectAttributes.addFlashAttribute("message", "Product removed from cart.");

        return "redirect:/cart"; 
    }
    
    
    @PostMapping("/cart/updateTotal")
    public String updateTotalPrice(HttpSession session, @RequestParam("totalPrice") Double totalPrice) {
        session.setAttribute("totalPrice", totalPrice);
        return "redirect:/cart";
    }

}
