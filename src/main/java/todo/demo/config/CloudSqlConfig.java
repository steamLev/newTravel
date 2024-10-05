package todo.demo.config;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudSqlConfig {

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:postgresql:///postgres?cloudSqlInstance=centralasiatravel:asia-northeast1:central-asia-database&socketFactory=com.google.cloud.sql.postgres.SocketFactory")
                .username("postgres")
                .password("postgres")
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}

