package com.library.config;

import com.library.entity.Member;
import com.library.entity.enums.MemberRole;
import com.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.setup.admin.username}")
    private String adminUsername;

    @Value("${app.setup.admin.password}")
    private String adminPassword;

    @Value("${app.setup.librarian.username}")
    private String librarianUsername;

    @Value("${app.setup.librarian.password}")
    private String librarianPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // Create Admin if not exists
        if (!memberRepository.existsByUsername(adminUsername)) {
            Member admin = Member.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .name("System Administrator")
                    .email("admin@library.com")
                    .phone("0999999999")
                    .address("Library HQ")
                    .role(MemberRole.ADMIN)
                    .isActive(true)
                    .build();
            memberRepository.save(admin);
            log.info("Default ADMIN account created: {} / {}", adminUsername, adminPassword);
        }

        // Create Librarian if not exists
        if (!memberRepository.existsByUsername(librarianUsername)) {
            Member librarian = Member.builder()
                    .username(librarianUsername)
                    .password(passwordEncoder.encode(librarianPassword))
                    .name("Main Librarian")
                    .email("librarian@library.com")
                    .phone("0888888888")
                    .address("Library Branch 1")
                    .role(MemberRole.LIBRARIAN)
                    .isActive(true)
                    .build();
            memberRepository.save(librarian);
            log.info("Default LIBRARIAN account created: {} / {}", librarianUsername, librarianPassword);
        }
    }
}
