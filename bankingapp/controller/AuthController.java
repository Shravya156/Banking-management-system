package com.shravya.bankingapp.controller;
import com.shravya.bankingapp.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import com.shravya.bankingapp.config.JwtUtil;
import com.shravya.bankingapp.entity.User;
import com.shravya.bankingapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthController(@Lazy UserService userService,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,UserRepository userRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userRepository=userRepository;
    }

    @PostMapping("/login")
// Change "String" to "ResponseEntity<?>" here:
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        User user = userService.findByEmail(loginUser.getEmail());

        // 1. Verification Logic
        if (!user.isEnabled()) {
            throw new RuntimeException("Please verify your email before logging in.");
        }

        if (!user.isAccountNonLocked()) {
            throw new RuntimeException("Account is blocked. Please verify OTP.");
        }

        if (!passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            userService.increaseFailedAttempts(user);
            throw new RuntimeException("Invalid password.");
        }

        // 2. Reset failures on success
        user.setFailedAttempts(0);
        userRepository.save(user);

        // 3. Generate Token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // 4. Create JSON Response
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getRole());

        // 5. Return the Object
        return ResponseEntity.ok(response);
    }
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
        userService.requestPasswordReset(email);
        return "Password reset OTP sent to your email.";
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {

        // Now we pass the OTP so the service can verify it before saving
        userService.resetPassword(email, otp, newPassword);

        return ResponseEntity.ok("Password has been reset successfully.");
    }
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<?> verifyResetOtp(@RequestParam String email, @RequestParam String otp) {
        // Reuse the verifyOtpOnly method we already wrote in UserService!
        boolean isValid = userService.verifyOtpOnly(email, otp);

        if (isValid) {
            return ResponseEntity.ok("OTP is valid.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP.");
        }
    }
}


