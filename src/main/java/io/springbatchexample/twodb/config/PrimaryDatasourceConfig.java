package io.springbatchexample.twodb.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"io.springbatchexample.twodb.db1"}, // 첫번째 DB가 있는 패키지(폴더) -> repository
        entityManagerFactoryRef = "primaryEntityManagerFactory", // EntityManager의 이름
        transactionManagerRef = "primaryTransactionManager" // 트랜잭션 매니저의 이름
)
public class PrimaryDatasourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.db1.datasource") // application.properties에 작성된 DB와 관련된 설정 값들의 접두사
    public DataSourceProperties primaryDatasourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.db1.datasource.configuration") // DB와 관련된 설정값들의 접두사에 .configuration 붙여준다. 이 어노테이션이 없으면 spring.db1.datasource.hikari로 시작하면됨
    public DataSource primaryDatasource() {
        return primaryDatasourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.db1.jpa")
    public JpaProperties db1JpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.db1.hibernate")
    public HibernateProperties db1HibernateProperties() {
        return new HibernateProperties();
    }

    @Bean(name = "primaryEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(EntityManagerFactoryBuilder builder, JpaProperties jpaProperties, HibernateProperties hibernateProperties) {
        Map<String, Object> properties = hibernateProperties.determineHibernateProperties(jpaProperties.getProperties(), new HibernateSettings());
        DataSource dataSource = primaryDatasource();
        return builder
                .dataSource(dataSource)
                .packages("io.springbatchexample.twodb.db1") // 첫번째 DB와 관련된 엔티티들이 있는 패키지(폴더) 경로
                .persistenceUnit("primaryEntityManager")
                .properties(properties)
                .build();
    }

    @Bean(name = "primaryTransactionManager")
    @Primary
    public PlatformTransactionManager primaryTransactionManager(
            final @Qualifier("primaryEntityManagerFactory") LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean
    ) {
        return new JpaTransactionManager(localContainerEntityManagerFactoryBean.getObject());
    }
}

