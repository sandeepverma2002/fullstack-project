package com.ecomerce.controller;

import com.ecomerce.model.User;
import com.ecomerce.model.UserDetails;
import com.ecomerce.model.Cart;
import com.ecomerce.model.Product;
import com.ecomerce.model.ShipmentDetails;
import com.ecomerce.repository.ShipmentDetailsRepository;
import com.ecomerce.service.CartService;
import com.ecomerce.service.ProductService;
import com.ecomerce.service.UserService;

import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PurchaseController {

    @Autowired
    private ShipmentDetailsRepository shipmentDetailsRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CartService cartService;

    @GetMapping("/purchase/{id}")
    public String showUserDetailsForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        Product product = productService.getProductById(id);
        if (product == null) {
            throw new RuntimeException("Product not found for id: " + id);
        }

        UserDetails userDetails = new UserDetails();
        userDetails.setName(loggedInUser.getFirstname());
        model.addAttribute("user", userDetails);
        model.addAttribute("product", product);

        return "/purchase/userDetailsForm";
    }

    @PostMapping("/purchase/confirm")
    public String confirmPurchase(
            @ModelAttribute UserDetails userDetails,
            @RequestParam("productId") Long productId,
            HttpSession session,
            Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        // Fetch the product
        Product product = productService.getProductById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found for id: " + productId);
        }

        ShipmentDetails shipmentDetails = new ShipmentDetails();
        shipmentDetails.setName(userDetails.getName());
        shipmentDetails.setAddress(userDetails.getAddress());
        shipmentDetails.setPincode(userDetails.getPincode());
        shipmentDetails.setMobilenumber(userDetails.getMobilenumber());
        shipmentDetails.setCountry(userDetails.getCountry());
        shipmentDetails.setState(userDetails.getState());
        shipmentDetails.setCity(userDetails.getCity());
        shipmentDetails.setProductId(productId);
        shipmentDetails.setUserId(loggedInUser.getId());

        shipmentDetailsRepository.save(shipmentDetails);

        double totalAmount = product.getPrice(); // Calculate total amount if needed
        return "redirect:/payment?totalAmount=" + totalAmount + "&productId=" + productId;
    }

   
    @GetMapping("/payment")
    public String showPaymentPage(
            @RequestParam("totalAmount") double totalAmount,
            @RequestParam("productId") Long productId,
            Model model) {

        Product product = productService.getProductById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found for id: " + productId);
        }

        model.addAttribute("product", product);
        model.addAttribute("totalAmount", totalAmount);

        return "/purchase/paymentPage";
    }

   
    @PostMapping("/payment/confirm")
    public String confirmPayment(
            @RequestParam("totalAmount") double totalAmount,
            HttpSession session,
            Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/users/login";
        }


        // Add a confirmation message
        model.addAttribute("message", "Your payment has been processed successfully!");
        model.addAttribute("totalAmount", totalAmount);

        return "/purchase/paymentConfirmation";
    }
    
    
    //cart pay page
    
    
 // Checkout cart items
    // Show User Details Form
    @GetMapping("purchase/shipment")
    public String showUserDetailsForm(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/users/login";
        }

        UserDetails userDetails = new UserDetails();
        model.addAttribute("userDetails", userDetails);

        Double totalPrice = cartService.calculateTotalPrice(loggedInUser);
        session.setAttribute("totalPrice", totalPrice);


        System.out.println(totalPrice);

        return "purchase/shipmentForm"; // This is the HTML template for the form
    }

    
    
    // Handle Form Submission and Redirect to Payment Page
    @PostMapping("purchase/cartBuy")
    public String handleUserDetailsForm(
        @ModelAttribute("userDetails") UserDetails userDetails,
        HttpSession session,
        Model model) {
        
        session.setAttribute("userDetails", userDetails);

        Double totalPrice = (Double) session.getAttribute("totalPrice");

        if (totalPrice == null) {
            throw new IllegalStateException("Total price is not set in the session.");
        }

        model.addAttribute("totalPrice", totalPrice);

        // Redirect to the payment page
        return "purchase/cartPayment";
    }



    @GetMapping("purchase/cartPayment")
    public String showCartPaymentPage(@ModelAttribute("totalAmount") Double totalAmount, Model model) {
        model.addAttribute("totalAmount", totalAmount);
        return "purchase/cartPayment"; 
    }

//    @PostMapping("/purchase/success")
//    public String handlePaymentSuccess(
//        @RequestParam("razorpay_payment_id") String paymentId,
//        @RequestParam("razorpay_order_id") String orderId,
//        @RequestParam("razorpay_signature") String signature,
//        Model model) {
//
//
//        model.addAttribute("paymentId", paymentId);
//        model.addAttribute("orderId", orderId);
//        model.addAttribute("signature", signature);
//        model.addAttribute("message", "Payment Successful!");
//        return "purchase/success";  
//    }

    
    
}
