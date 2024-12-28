package com.ecomerce.config;

import com.razorpay.RazorpayClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    private final String KEY_ID = "rzp_test_tCGAc7awzqACRR";
    private final String KEY_SECRET = "38rRxytAuEVG5xGqpJatyHhz";

    @Bean
    public RazorpayClient razorpayClient() throws Exception {
        return new RazorpayClient(KEY_ID, KEY_SECRET);
    }
}