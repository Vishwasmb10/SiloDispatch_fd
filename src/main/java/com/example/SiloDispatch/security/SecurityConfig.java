package com.example.SiloDispatch.security;

import com.example.SiloDispatch.models.AppUser;
import com.example.SiloDispatch.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/login.html", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/driver.html").hasRole("DRIVER")
                        .requestMatchers("/", "/index.html").hasRole("MANAGER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")  // Important!
                        .successHandler(roleBasedRedirectHandler())
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedRedirectHandler() {
        return (request, response, authentication) -> {
            // Fetch user from DB
            String username = authentication.getName();
            AppUser dbUser = userRepository.findByUsername(username).orElse(null);

            if (dbUser != null && "ROLE_DRIVER".equals(dbUser.getRole())) {
                // Store driverId in session
                request.getSession().setAttribute("driverId", dbUser.getDriverId());
                response.sendRedirect("/driver.html");
            } else if ("ROLE_MANAGER".equals(dbUser.getRole())) {
                response.sendRedirect("/");
            } else {
                response.sendRedirect("/login?error=unauthorized");
            }
        };
    }



//    @Bean
//    public UserDetailsService userDetailsService() {
//        return userDetailsService;
//    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
