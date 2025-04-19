package com.springboot.user_mgmt_app.repository;

import com.springboot.user_mgmt_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}