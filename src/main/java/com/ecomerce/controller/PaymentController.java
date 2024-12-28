package com.ecomerce.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.codec.digest.HmacUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private RazorpayClient razorpayClient;

    @Value("${razorpay.keyId}")
    private String razorpayKeyId;

    @Value("${razorpay.secretKey}")
    private String razorpaySecretKey;

    @PostMapping("/payment/razorpay")
    public String createRazorpayOrder(HttpSession session, Model model) {
        try {
            Double totalPrice = (Double) session.getAttribute("totalPrice");

            if (totalPrice == null) {
                throw new IllegalStateException("Total price not found in session.");
            }

            // Create Razorpay order
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (totalPrice * 100));  
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
            orderRequest.put("payment_capture", 1);

            // Create order on Razorpay
            Order order = razorpayClient.orders.create(orderRequest);

            // Send Razorpay order ID to frontend
            model.addAttribute("orderId", order.get("id"));
            model.addAttribute("totalPrice", totalPrice);
            model.addAttribute("razorpayKey", razorpayKeyId);  
            System.out.println("Razorpay Key: " + razorpayKeyId);

            return "payment/paymentPage";
        } catch (Exception e) {
            logger.error("Error creating Razorpay order: ", e);
            model.addAttribute("error", "Error creating Razorpay order: " + e.getMessage());
            return "error"; 
        }
    }
    
    // Handle Razorpay payment success
    @PostMapping("/payment/success")
    public String handlePaymentSuccess(
            Model model,
            String razorpayPaymentId,
            String razorpayOrderId,
            String razorpaySignature,
            HttpSession session) {

        try {
            String data = razorpayOrderId + "|" + razorpayPaymentId;

            String generatedSignature = HmacUtils.hmacSha256Hex(razorpaySecretKey, data);

            if (!generatedSignature.equals(razorpaySignature)) {
                throw new Exception("Signature mismatch. Payment may be fraudulent.");
            }

            model.addAttribute("message", "Payment Success! Razorpay Payment ID: " + razorpayPaymentId);
            return "payment/success";  
        } catch (Exception e) {
            logger.error("Payment verification failed: ", e);
            model.addAttribute("error", "Payment verification failed: " + e.getMessage());
            return "error";
        }
    }
}
