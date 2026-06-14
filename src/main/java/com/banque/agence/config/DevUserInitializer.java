package com.banque.agence.config;

import com.banque.agence.domain.entity.User;
import com.banque.agence.domain.enums.UserRole;
import com.banque.agence.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DevUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.save(new User(
                "admin",
                passwordEncoder.encode("admin123"),
                "Administrateur Système",
                "admin@banque.local",
                UserRole.ADMIN
        ));
        userRepository.save(new User(
                "agent",
                passwordEncoder.encode("agent123"),
                "Agent Bancaire",
                "agent@banque.local",
                UserRole.AGENT
        ));
        userRepository.save(new User(
                "chef",
                passwordEncoder.encode("chef123"),
                "Chef d'Agence",
                "chef@banque.local",
                UserRole.CHEF_AGENCE
        ));
    }
}
