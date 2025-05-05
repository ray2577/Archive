package com.ray.archive.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
    basePackages = "com.ray.archive.repository",
    entityManagerFactoryRef = "customEntityManagerFactory",
    transactionManagerRef = "customTransactionManager"
)
@EnableTransactionManagement
public class JpaConfig {
    
    @Primary
    @Bean(name = "customEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean customEntityManagerFactory(
            @Qualifier("dataSource") DataSource dataSource) {
        
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.ray.archive.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        em.setJpaVendorAdapter(vendorAdapter);
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);
        
        em.setJpaPropertyMap(properties);
        
        return em;
    }
    
    @Primary
    @Bean(name = "customTransactionManager")
    public PlatformTransactionManager customTransactionManager(
            @Qualifier("customEntityManagerFactory") EntityManagerFactory customEntityManagerFactory) {
        return new JpaTransactionManager(customEntityManagerFactory);
    }
} 