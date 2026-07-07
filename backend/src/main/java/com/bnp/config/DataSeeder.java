package com.bnp.config;

import com.bnp.common.enums.Enums.AccountStatus;
import com.bnp.common.enums.Role;
import com.bnp.subscription.SubscriptionService;
import com.bnp.user.User;
import com.bnp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Seeds the default super admin and pricing plans on first startup. */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;

    @Value("${bnp.admin.email}")
    private String adminEmail;

    @Value("${bnp.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        Optional<User> existing = userRepository.findByEmail(adminEmail);
        if (existing.isEmpty()) {
            userRepository.save(User.builder()
                    .fullName("Super Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.SUPER_ADMIN)
                    .status(AccountStatus.ACTIVE)
                    .emailVerified(true)
                    .build());
            System.out.println(">> Seeded super admin: " + adminEmail);
        } else {
            // Always keep the admin password in sync with application.properties
            User admin = existing.get();
            if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode(adminPassword));
                userRepository.save(admin);
                System.out.println(">> Updated super admin password: " + adminEmail);
            }
        }

        subscriptionService.seedDefaultPlans();
    }
}
