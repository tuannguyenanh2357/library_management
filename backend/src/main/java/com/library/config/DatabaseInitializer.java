package com.library.config;

import com.library.entity.Member;
import com.library.entity.enums.MemberRole;
import com.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;

    @Value("${app.setup.admin.username}")
    private String adminUsername;

    @Value("${app.setup.admin.password}")
    private String adminPassword;

    @Value("${app.setup.librarian.username}")
    private String librarianUsername;

    @Value("${app.setup.librarian.password}")
    private String librarianPassword;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initStoredProcedures();

        // Tạo tài khoản admin
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

        // Tạo tài khoản Librarian
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

    private void initStoredProcedures() {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("sql/stored_procedures.sql"));
            populator.setSeparator("GO");
            populator.execute(dataSource);
            log.info("Stored Procedures initialized successfully from sql/stored_procedures.sql.");
        } catch (Exception e) {
            log.error("Failed to initialize Stored Procedures: {}", e.getMessage(), e);
        }
    }
}

