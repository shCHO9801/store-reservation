package com.zerobase.storereservation.config;

import com.zerobase.storereservation.entity.User;
import com.zerobase.storereservation.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserTestConfig {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            User user = new User();
            user.setUsername("zerobase");
            user.setPassword("zerobase");
            user.setRole("CUSTOMER");
            userRepository.save(user);
            userRepository.findAll().forEach(System.out::println);
        };
    }
}
