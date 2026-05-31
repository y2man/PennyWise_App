package com.pennywise.controller;
import com.pennywise.config.JwtUtil; import com.pennywise.model.User;
import com.pennywise.repository.UserRepository; import com.pennywise.service.EmailService;
import jakarta.validation.Valid; import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.security.SecureRandom; import java.time.LocalDateTime; import java.util.Map;
@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepo; private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil; private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();
    public AuthController(UserRepository ur, PasswordEncoder enc, JwtUtil jwt, EmailService es) {
        userRepo = ur; encoder = enc; jwtUtil = jwt; emailService = es;
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail()))
            return ResponseEntity.badRequest().body(Map.of("error","Email already registered"));
        User u = new User(); u.setEmail(req.getEmail()); u.setName(req.getName());
        u.setPassword(encoder.encode(req.getPassword())); userRepo.save(u);
        return ResponseEntity.ok(Map.of("token",jwtUtil.generateToken(u.getEmail()),"email",u.getEmail(),"name",u.getName()));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        User u = userRepo.findByEmail(req.getEmail()).orElse(null);
        if (u == null || !encoder.matches(req.getPassword(), u.getPassword()))
            return ResponseEntity.status(401).body(Map.of("error","Invalid email or password"));
        String name = u.getName() != null ? u.getName() : u.getEmail().split("@")[0];
        return ResponseEntity.ok(Map.of("token",jwtUtil.generateToken(u.getEmail()),"email",u.getEmail(),"name",name));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgot(@RequestBody Map<String,String> body) {
        String email = body.get("email");
        if (email == null || !email.contains("@")) return ResponseEntity.badRequest().body(Map.of("error","Invalid email"));
        User u = userRepo.findByEmail(email).orElse(null);
        if (u == null) return ResponseEntity.ok(Map.of("message","If registered, a code was sent."));
        String otp = String.format("%06d", random.nextInt(900000)+100000);
        u.setOtpCode(otp); u.setOtpExpiry(LocalDateTime.now().plusMinutes(10)); userRepo.save(u);
        emailService.sendOtp(email, otp, u.getName() != null ? u.getName() : email.split("@")[0]);
        return ResponseEntity.ok(Map.of("message","Code sent."));
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verify(@RequestBody Map<String,String> body) {
        User u = userRepo.findByEmail(body.get("email")).orElse(null);
        if (u == null) return ResponseEntity.badRequest().body(Map.of("error","User not found"));
        if (u.getOtpCode() == null || !u.getOtpCode().equals(body.get("code")))
            return ResponseEntity.badRequest().body(Map.of("error","Incorrect code"));
        if (u.getOtpExpiry() == null || LocalDateTime.now().isAfter(u.getOtpExpiry()))
            return ResponseEntity.badRequest().body(Map.of("error","Code expired"));
        return ResponseEntity.ok(Map.of("resetToken", jwtUtil.generateToken("RESET:"+body.get("email"))));
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> reset(@RequestBody Map<String,String> body) {
        String t = body.get("resetToken");
        if (t == null || !jwtUtil.isValid(t)) return ResponseEntity.badRequest().body(Map.of("error","Invalid token"));
        String sub = jwtUtil.extractEmail(t);
        if (!sub.startsWith("RESET:")) return ResponseEntity.badRequest().body(Map.of("error","Wrong token type"));
        User u = userRepo.findByEmail(sub.substring(6)).orElse(null);
        if (u == null) return ResponseEntity.badRequest().body(Map.of("error","User not found"));
        u.setPassword(encoder.encode(body.get("newPassword"))); u.setOtpCode(null); u.setOtpExpiry(null);
        userRepo.save(u);
        return ResponseEntity.ok(Map.of("message","Password updated"));
    }
    public static class RegisterRequest {
        @NotBlank private String name; @Email @NotBlank private String email; @Size(min=6) @NotBlank private String password;
        public String getName(){return name;} public void setName(String n){name=n;}
        public String getEmail(){return email;} public void setEmail(String e){email=e;}
        public String getPassword(){return password;} public void setPassword(String p){password=p;}
    }
    public static class LoginRequest {
        @Email @NotBlank private String email; @NotBlank private String password;
        public String getEmail(){return email;} public void setEmail(String e){email=e;}
        public String getPassword(){return password;} public void setPassword(String p){password=p;}
    }
}
