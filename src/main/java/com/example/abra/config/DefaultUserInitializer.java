package com.example.abra.config;

import com.example.abra.models.UserModel;
import com.example.abra.repositories.UserModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultUserInitializer implements ApplicationRunner {

    private final UserModelRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${default.user.enabled:false}")
    private boolean enabled;

    @Value("${default.user.login:}")
    private String defaultLogin;

    @Value("${default.user.password:}")
    private String defaultPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        if (defaultLogin == null || defaultLogin.isBlank() || defaultPassword == null || defaultPassword.isBlank()) {
            log.warn("Default user seeding is enabled but login/password are not set. Skipping.");
            return;
        }

        if (userRepository.existsByLogin(defaultLogin)) {
            log.info("Default user '{}' already exists.", defaultLogin);
            return;
        }

        UserModel user = UserModel.builder()
            .login(defaultLogin)
            .password(passwordEncoder.encode(defaultPassword))
            .build();
        userRepository.save(user);
        log.info("Default user '{}' has been created.", defaultLogin);
    }
}
