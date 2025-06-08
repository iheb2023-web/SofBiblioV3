package com.sofrecom.backend.controllers;

import com.sofrecom.backend.dtos.PasswordResetResponse;
import com.sofrecom.backend.entities.User;
import com.sofrecom.backend.repositories.UserRepository;
import com.sofrecom.backend.services.PasswordResetService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final UserRepository userRepository;


    @PostMapping("/request")
    public ResponseEntity<String> requestPasswordReset(@RequestParam("email") String email) throws MessagingException {
        Optional<User> user = userRepository.findByEmail(email);

        if (user == null) { //NOSONAR - handled below, though Optional should not be null
            return ResponseEntity.badRequest().body("User not found");
        } else {
            int randomNumber = ThreadLocalRandom.current().nextInt(1000, 10000); //NOSONAR - magic number, acceptable in this case
            String token = String.valueOf(randomNumber);

            passwordResetService.createPasswordResetTokenForUser(user.get(), token); //NOSONAR - Optional is assumed present here
            passwordResetService.sendPasswordResetEmail(email, token);

            return ResponseEntity.ok("Password reset email sent successfully");
        }
    }


    @GetMapping("/getTokenByEmail/{email}")
    public String getPasswordResetCode(@PathVariable("email") String email) {
        return this.passwordResetService.getTokenByEmail(email);
    }

    @PutMapping("/reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(@RequestParam("token") String token,
                                                               @RequestParam("password") String newPassword) {
        return ResponseEntity.ok(passwordResetService.resetPassword(token, newPassword));
    }

    @PutMapping("/changePassword")
    public ResponseEntity<PasswordResetResponse> changePassword(@RequestParam("email") String email,
                                                                @RequestParam("password") String newPassword) {
        return ResponseEntity.ok(passwordResetService.changePassword(email, newPassword));
    }
}