package com.backend.tutor_app.security;



import com.backend.tutor_app.model.Admin;
import com.backend.tutor_app.model.enums.Role;
import com.backend.tutor_app.repositories.UserRepository;
import com.k48.stock_management_system.model.Administrateur;
import com.k48.stock_management_system.model.Role;
import com.k48.stock_management_system.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class InitAdminConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            if (userRepository.count() == 0) {
                Admin admin = new Admin();
                admin.setFirstName("Super");
                admin.setLastName("Admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123")); // password hashé
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
                System.out.println("✅ Admin créé avec succès: email=admin@example.com, password=admin123");
            }
        };
    }
}
