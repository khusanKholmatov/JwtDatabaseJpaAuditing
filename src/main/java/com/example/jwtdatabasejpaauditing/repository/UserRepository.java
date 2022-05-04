package com.example.jwtdatabasejpaauditing.repository;

import com.example.jwtdatabasejpaauditing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.Email;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(@Email String email);
    Optional<User> findByEmailAndEmailCode(@Email String email, String emailCode);

    Optional<User> findByEmail(@Email String email);
}
