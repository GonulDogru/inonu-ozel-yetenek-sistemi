package tr.edu.inonu.oys.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.edu.inonu.oys.config.JwtService;
import tr.edu.inonu.oys.dto.AuthResponse;
import tr.edu.inonu.oys.dto.LoginRequest;
import tr.edu.inonu.oys.dto.RegisterRequest;
import tr.edu.inonu.oys.dto.UserDTO;
import tr.edu.inonu.oys.model.Role;
import tr.edu.inonu.oys.model.User;
import tr.edu.inonu.oys.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = new User();
            user.setUsername(request.username());
            user.setPassword(request.password());
            user.setFirstName(request.firstName());
            user.setLastName(request.lastName());
            user.setRole(Role.APPLICANT);
            return ResponseEntity.ok(new UserDTO(userService.registerUser(user)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.loginUser(request.username(), request.password());
            return ResponseEntity.ok(new AuthResponse(jwtService.createToken(user), new UserDTO(user)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
