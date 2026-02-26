package org.opendevstack.apiservice.core.config;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Async configuration for audit log persistence. Provides a dedicated thread pool so
 * audit writes do not block API response threads.
 */
@Slf4j
@Configuration
@EnableAsync
public class AuditAsyncConfig implements AsyncConfigurer {

	@Bean(name = "auditTaskExecutor")
	public Executor auditTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(5);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("audit-");
		executor.setRejectedExecutionHandler((runnable, pool) -> log
			.warn("Audit task rejected — thread pool queue is full. Audit entry will be lost."));
		executor.initialize();
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (Throwable ex, Method method, Object... params) -> log
			.error("Async audit error in {}: {}", method.getName(), ex.getMessage(), ex);
	}

}
