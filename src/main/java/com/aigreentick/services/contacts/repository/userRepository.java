package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface userRepository extends JpaRepository<User, Long> {

}
