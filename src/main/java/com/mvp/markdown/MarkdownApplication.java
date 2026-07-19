package com.mvp.markdown;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class MarkdownApplication {

	public static void main(String[] args) {

        SpringApplication.run(MarkdownApplication.class, args);
	}

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void testConnection() {
        System.out.println("Testing DB connection...");
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        System.out.println("Connection successful! Result: " + result);
    }

}
