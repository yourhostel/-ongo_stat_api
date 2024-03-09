package com.example.stat.controller;

import com.example.stat.dto.JwtResponse;
import com.example.stat.dto.LoginDto;
import com.example.stat.dto.UserDto;
import com.example.stat.model.User;
import com.example.stat.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto) {
        User user = userService.registerNewUserAccount(userDto);
        return ResponseEntity
                .ok(String.format("User %s with role %s registered successfully!",
                user.getUsername(),
                user.getRolesStr()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginDto loginDto) {
        String token = userService.login(loginDto.getUsername(), loginDto.getPassword());
        return ResponseEntity.ok(new JwtResponse(token));
    }

}
