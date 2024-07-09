package com.viettel.importwiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = {GsonAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@EnableCaching
public class ImportWizApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(ImportWizApplication.class, args);
    }
}
