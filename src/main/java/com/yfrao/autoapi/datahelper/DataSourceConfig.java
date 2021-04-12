package com.yfrao.autoapi.datahelper;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class DataSourceConfig {
    @Bean(name= "mesfDataSource")
    @Qualifier("mesfDataSource")
    @Primary
    @ConfigurationProperties(prefix= "spring.datasource.mesf")
    public DataSource mesfDataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "mesfJdbcTemplate")
    public JdbcTemplate mesfJdbcTemplate(@Qualifier("mesfDataSource") DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }
}
