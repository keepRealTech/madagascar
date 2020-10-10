package com.keepreal.madagascar.workflow.statistics.config;

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
        entityManagerFactoryRef = "entityManagerFactoryCoua",
        transactionManagerRef = "transactionManagerCoua",
        basePackages = {"com.keepreal.madagascar.workflow.statistics.repository.coua"}
)
public class CouaDatasourceConfig {

    private final DataSource datasource;
    private final JpaProperties jpaProperties;
    private final HibernateProperties hibernateProperties;

    public CouaDatasourceConfig(@Qualifier("couaDatasource") DataSource datasource,
                                JpaProperties jpaProperties,
                                HibernateProperties hibernateProperties) {
        this.datasource = datasource;
        this.jpaProperties = jpaProperties;
        this.hibernateProperties = hibernateProperties;
    }

    @Primary
    @Bean(name = "entityManagerCoua")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
        return entityManagerFactoryCoua(builder).getObject().createEntityManager();
    }

    @Primary
    @Bean(name = "entityManagerFactoryCoua")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryCoua(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(datasource)
                .properties(getVendorProperties(datasource))
                .packages("com.keepreal.madagascar.workflow.statistics.model.coua")
                .persistenceUnit("primaryPersistenceUnit")
                .build();
    }

    @Bean(name = "transactionManagerCoua")
    PlatformTransactionManager transactionManagerCoua(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(entityManagerFactoryCoua(builder).getObject());
    }

    private Map<String, Object> getVendorProperties(DataSource dataSource) {
        return this.hibernateProperties.determineHibernateProperties(this.jpaProperties.getProperties(), new HibernateSettings());
    }
}
