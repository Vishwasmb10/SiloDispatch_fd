package com.example.SiloDispatch.util;

import com.razorpay.RazorpayClient;
import lombok.Getter;
import lombok.Setter;

public class RazorpayClientHolder {
    @Setter
    @Getter
    private static RazorpayClient client;



}
