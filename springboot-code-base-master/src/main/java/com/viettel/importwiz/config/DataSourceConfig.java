package com.viettel.importwiz.config;

import com.viettel.security.PassTranformer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;


@Configuration
public class DataSourceConfig {

    @Value("${viettel-security.key}")
    private String key;

    @Value("${spring.datasource.url}")
    private String mariaDbUrl;
    @Value("${spring.datasource.username}")
    private String mariaDbUsername;

    @Value("${spring.datasource.password}")
    private String mariaDbPassword;

    @Value("${spring.presto-datasource.username}")
    private String prestoUserName;

    @Value("${spring.presto-datasource.password}")
    private String prestoPassword;

    @Value("${spring.presto-datasource.jdbc-url}")
    private String prestoUrl;

    @Value("${spring.presto-datasource.driver-class-name}")
    private String prestoDriverClassName;

    @Value("${spring.presto-datasource.SSLTrustStorePath}")
    private String prestoTrustStorePath;

    @Value("${spring.presto-datasource.SSLTrustStorePassword}")
    private String prestoSSLTrustStorePassword;

    @Value("${spring.presto-datasource.poolSize}")
    private Integer prestoMaxPoolSize;

    @Bean
    @Primary
    public DataSource getDataSource() {
        PassTranformer.setInputKey(key);
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(PassTranformer.decrypt(mariaDbUrl));
        dataSourceBuilder.username(PassTranformer.decrypt(mariaDbUsername));
        dataSourceBuilder.password(PassTranformer.decrypt(mariaDbPassword));
        
        return dataSourceBuilder.build();
    }

    @Bean
    @Profile("prod")
    public DataSource prestoDS() {
        HikariConfig config = new HikariConfig();
        PassTranformer.setInputKey(key);
        config.setUsername(PassTranformer.decrypt(prestoUserName));
        config.setPassword(PassTranformer.decrypt(prestoPassword));
        config.setJdbcUrl(PassTranformer.decrypt(prestoUrl));
        config.setDriverClassName(prestoDriverClassName);
        config.addDataSourceProperty("SSL", true);
        config.addDataSourceProperty("SSLTrustStorePath", prestoTrustStorePath);
        config.addDataSourceProperty("SSLTrustStorePassword", PassTranformer.decrypt(prestoSSLTrustStorePassword));
        config.setMaximumPoolSize(prestoMaxPoolSize);

        return new HikariDataSource(config);
    }
}
