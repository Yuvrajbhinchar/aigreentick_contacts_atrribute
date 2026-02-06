package com.aigreentick.services.contacts.service;


import com.aigreentick.services.contacts.repository.userRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class userService {

    private final userRepository userRepository;


}
