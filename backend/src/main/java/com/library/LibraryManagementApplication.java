package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class LibraryManagementApplication {

	@Bean
	public LockProvider lockProvider(DataSource dataSource) {
		return new JdbcTemplateLockProvider(
				JdbcTemplateLockProvider.Configuration.builder()
						.withJdbcTemplate(new JdbcTemplate(dataSource))
						.usingDbTime() // Works with SQL Server
						.build()
		);
	}
	public static void main(String[] args) {
		SpringApplication.run(LibraryManagementApplication.class, args);
	}

}
