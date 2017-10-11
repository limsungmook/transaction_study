package com.sungmook.transaction.without_transaction;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "com.sungmook.transaction")
@EnableJpaRepositories(basePackages = "com.sungmook.transaction")
public class TestConfiguration {


}

