package com.keepreal.madagascar.workflow.support_activity.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactoryVanga",
        transactionManagerRef = "transactionManagerVanga",
        basePackages = {"com.keepreal.madagascar.workflow.support_activity.repository.vanga"}
)
public class VangaDatasourceConfig {

    private final DataSource datasource;
    private final JpaProperties jpaProperties;
    private final HibernateProperties hibernateProperties;

    public VangaDatasourceConfig(@Qualifier("vangaDatasource") DataSource datasource,
                                 JpaProperties jpaProperties,
                                 HibernateProperties hibernateProperties) {
        this.datasource = datasource;
        this.jpaProperties = jpaProperties;
        this.hibernateProperties = hibernateProperties;
    }

    @Primary
    @Bean(name = "entityManagerVanga")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
        return entityManagerFactoryVanga(builder).getObject().createEntityManager();
    }

    @Primary
    @Bean(name = "entityManagerFactoryVanga")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryVanga(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(datasource)
                .properties(getVendorProperties(datasource))
                .packages("com.keepreal.madagascar.workflow.support_activity.model.vanga")
                .persistenceUnit("primaryPersistenceUnit")
                .build();
    }

    @Bean(name = "transactionManagerVanga")
    PlatformTransactionManager transactionManagerVanga(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(entityManagerFactoryVanga(builder).getObject());
    }

    private Map<String, Object> getVendorProperties(DataSource dataSource) {
        return this.hibernateProperties.determineHibernateProperties(this.jpaProperties.getProperties(), new HibernateSettings());
    }
}
