package com.springboot.user_mgmt_app.controller;

import com.springboot.user_mgmt_app.model.User;
import com.springboot.user_mgmt_app.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Controller", description = "APIs for managing users")
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        return userService.getUserById(id)
                .map(user -> {
                    log.info("User found: {}", user);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    log.warn("User not found with id: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Creating user: {}", user);
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        log.info("Updating user with id: {}", id);
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            log.info("User updated successfully: {}", updatedUser);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            log.error("Error updating user with id: {} - {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        try {
            userService.deleteUser(id);
            log.info("User deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting user with id: {} - {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}