package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUuid(String uuid);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUuid(String uuid);

    Optional<User> findByPasswordResetTokenHash(String tokenHash);

    Optional<User> findByEmailVerificationTokenHash(String tokenHash);
}