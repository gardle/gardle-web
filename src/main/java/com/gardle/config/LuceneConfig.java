package com.gardle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Configuration
public class LuceneConfig {
    @Bean
    public LuceneIndexServiceBean luceneIndexServiceBean(EntityManagerFactory entityManagerFactory) {
        LuceneIndexServiceBean luceneIndexServiceBean = new LuceneIndexServiceBean(entityManagerFactory);
        luceneIndexServiceBean.triggerIndexing();
        return luceneIndexServiceBean;
    }
}
