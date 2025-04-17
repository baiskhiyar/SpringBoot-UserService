package UserService.controllers;

import UserService.models.Users;
import UserService.services.UserService;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("spring/users")
public class UserController {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("healthCheck")
    public ResponseEntity<?> healthCheck() {
        logger.info("Health check initiated!");
        logger.error("Health check completed!");
        Map<String, Object> response = new HashMap<>();
        response.put("data", "User Service is up and running");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @PostMapping("register")
    public ResponseEntity<?> registerUser(@RequestBody Users user) {
        try {
            return new ResponseEntity<>(userService.registerUser(user), HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("updateUser")
    @PreAuthorize("hasAnyAuthority('admin', 'create-user')")
    public  ResponseEntity<?> updateUser(@RequestBody Users user) {
        try {
            return new ResponseEntity<>(userService.updateUserById(user.getId(), user), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("getByUsername/{username}")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        Users user = userService.findByUsername(username);
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("getUser")
    public ResponseEntity<?> getUserById(@RequestParam(name = "userId") Integer userId) {
        Users user = userService.findById(userId);
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("login")
    public String loginUser(@RequestBody Map<String, Object> payload) {
        Object username = payload.get("username");
        Object password = payload.get("password");
        return userService.loginUser((String) username, (String) password);
    }

    @DeleteMapping("logout")
    public  ResponseEntity<?> logoutUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>("Missing or invalid Authorization header", HttpStatus.BAD_REQUEST);
        }
        String authToken = authorizationHeader.substring(7); // Removing "Bearer " from authorizationHeader.
        String response = userService.logoutUser(authToken);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("getUserDetailsForLogin")
    public ResponseEntity<?> getUserDetailsForLogin(
            @RequestParam(name = "username") String username,
            @RequestParam(name = "password") String password
    ) {
        try{
            Users userDetails = userService.getUserDetailsForLogin(username, password);
            return new ResponseEntity<>(userDetails, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
