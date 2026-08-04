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

	// Cấp Ổ khóa ShedLock để khóa cửa Cronjob, tránh chạy trùng lặp khi có nhiều
	// Server
	@Bean
	public LockProvider lockProvider(DataSource dataSource) {
		return new JdbcTemplateLockProvider(
				JdbcTemplateLockProvider.Configuration.builder()
						.withJdbcTemplate(new JdbcTemplate(dataSource)) // Chỉ đường cho ShedLock cất chìa khóa vào SQL
																		// Server (bảng shedlock)
						.usingDbTime() // dùng Đồng hồ của SQL Server làm chuẩn (chống lệch giờ giữa các máy chủ)
						.build());
	}

	public static void main(String[] args) {
		SpringApplication.run(LibraryManagementApplication.class, args);
	}

}
