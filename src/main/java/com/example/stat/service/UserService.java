package com.example.stat.service;

import com.example.stat.dto.UserDto;
import com.example.stat.exception.IncorrectPasswordException;
import com.example.stat.exception.UserAlreadyExistException;
import com.example.stat.model.User;
import com.example.stat.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final MongoTemplate mongoTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    public User registerNewUserAccount(UserDto userDto) {
        // Использование MongoTemplate для проверки существования пользователя
        Query query = new Query(Criteria.where("username").is(userDto.getUsername()));
        boolean userExists = mongoTemplate.exists(query, User.class);

        if (userExists) {
            throw new UserAlreadyExistException("There is an account with that username: " + userDto.getUsername());
        }

        User newUser = new User();
        newUser.setUsername(userDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        newUser.setRoles(new String[]{"USER"});

        return mongoTemplate.save(newUser);
    }

    public String login(String username, String password) {
        UserDetails userDetails = loadUserByUsername(username);

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new IncorrectPasswordException("Incorrect password for username: " + username);
        }

        return jwtUtil.generateToken(userDetails);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findUserByUsername(username);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                // TODO: Set roles and authorities for the user if needed
                .build();
    }

    public User findUserByUsername(String username) {
        return Optional.ofNullable(mongoTemplate
                        .findOne(new Query(Criteria.where("username")
                                .is(username)), User.class))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }


}
