package com.example.SiloDispatch.config;

import com.example.SiloDispatch.util.RazorpayClientHolder;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    @Value("${razorpay.key_id}")
    private String key;

    @Value("${razorpay.key_secret}")
    private String secret;

    @PostConstruct
    public void init() throws RazorpayException {
        RazorpayClient client = new RazorpayClient(key, secret);
        RazorpayClientHolder.setClient(client);
    }
}
