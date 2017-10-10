package com.sungmook.transaction.async_sync_manager;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "com.sungmook.transaction")
@EnableJpaRepositories(basePackages = "com.sungmook.transaction")
public class TestConfiguration {


}

