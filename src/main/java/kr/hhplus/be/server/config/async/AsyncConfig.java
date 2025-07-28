package kr.hhplus.be.server.config.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "logExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-log");
        executor.initialize();
        return executor;
    }

    @Bean
    public AsyncUncaughtExceptionHandler asyncExceptionHandler() {
        return new AsyncExceptionHandler();
    }

    @Component
    public static class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(AsyncExceptionHandler.class);

        @Override
        public void handleUncaughtException(Throwable e, Method method, Object... params) {
            log.error("Async method execution failed. Method: {}, Exception: {}", method.getName(), e.getMessage(), e);
        }
    }


}
