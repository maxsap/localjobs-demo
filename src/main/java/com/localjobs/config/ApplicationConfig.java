package com.localjobs.config;

import javax.inject.Inject;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.localjobs.domain.Account;
import com.localjobs.jpa.repository.AccountRepository;
import com.localjobs.mongodb.repository.JobRepository;

@ComponentScan(basePackages = "com.localjobs")
@Configuration
@PropertySource("classpath:application.properties")
@EnableJpaRepositories(basePackageClasses = AccountRepository.class)
@EnableMongoRepositories(basePackageClasses = JobRepository.class)
@EnableTransactionManagement
@EnableCaching
public class ApplicationConfig {

    @Inject
    private DatasourceConfig datasourceConfig;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabase(datasourceConfig.database());
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(true);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(Account.class.getPackage().getName());
        factory.setDataSource(datasourceConfig.dataSource());

        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {

        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return txManager;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(datasourceConfig.mongoDbFactory());
        return mongoTemplate;
    }

    @Bean
    public RedisTemplate<String, Account> redisTemplate() {
        RedisTemplate<String, Account> template = new RedisTemplate<>();
        template.setConnectionFactory(datasourceConfig.redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(Account.class));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new JacksonJsonRedisSerializer<>(Account.class));
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager() {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate());
        return cacheManager;
    }
}
