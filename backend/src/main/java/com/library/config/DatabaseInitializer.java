package com.library.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initStoredProcedures();
    }

    private void initStoredProcedures() {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("sql/stored_procedures.sql"));
            populator.setSeparator("GO");
            populator.execute(dataSource);
            log.info("Thủ tục lưu trữ đã được khởi tạo thành công từ sql/stored_procedures.sql.");
        } catch (Exception e) {
            log.error("Không thể khởi tạo thủ tục lưu trữ.: {}", e.getMessage(), e);
        }
    }
}
