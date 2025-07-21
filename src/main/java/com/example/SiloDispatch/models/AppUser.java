package com.example.SiloDispatch.models;

import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;

//@Getter
@Data
public class AppUser {
    @Id
    private Long id;
    private String username;
    private String password;
    private String role; // e.g., "ROLE_MANAGER" or "ROLE_DRIVER"
    private boolean enabled;
    private Long driverId;
    // Getters and Setters
}
