package com.library.config;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class JasperReportConfig {

    @Bean
    public JasperReport revenueReportTemplate() throws Exception {
        return compileReport("/reports/revenue_report.jrxml");
    }

    @Bean
    public JasperReport topOffendersSubReport() throws Exception {
        return compileReport("/reports/top_offenders_sub.jrxml");
    }

    @Bean
    public JasperReport topBooksSubReport() throws Exception {
        return compileReport("/reports/top_books_sub.jrxml");
    }

    private JasperReport compileReport(String path) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                throw new RuntimeException("Report template not found: " + path);
            }
            return JasperCompileManager.compileReport(stream);
        }
    }
}
