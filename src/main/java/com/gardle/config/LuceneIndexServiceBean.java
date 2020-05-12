package com.gardle.config;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;

public class LuceneIndexServiceBean {
    private final static Logger LOGGER = LoggerFactory.getLogger(LuceneIndexServiceBean.class);

    private final FullTextEntityManager fullTextEntityManager;

    public LuceneIndexServiceBean(EntityManagerFactory entityManagerFactory) {
        fullTextEntityManager = Search.getFullTextEntityManager(entityManagerFactory.createEntityManager());
    }

    public void triggerIndexing() {
        try {
            fullTextEntityManager.createIndexer().startAndWait();
            LOGGER.debug("Lucene index created");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
