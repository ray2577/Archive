package com.ray.archive.config;

import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {
    
    /**
     * This bean contributes to the Hibernate metadata building process.
     * We're using an empty implementation since we now rely on standard
     * JPA annotations and proper Java types for all entity fields.
     */
    @Bean
    public MetadataBuilderContributor metadataBuilderContributor() {
        return metadataBuilder -> {
            // Empty implementation - relies on standard JPA annotations
        };
    }
} 