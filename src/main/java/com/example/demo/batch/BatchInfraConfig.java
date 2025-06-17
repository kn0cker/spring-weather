package com.example.demo.batch;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BatchInfraConfig {

    @Bean
    public ThreadPoolTaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setThreadNamePrefix("batch-");
        t.setCorePoolSize(4);
        t.setMaxPoolSize(10);
        t.initialize();
        return t;
    }

    @Primary
    @Bean(name = "asyncJobLauncher")
    public JobLauncher asyncJobLauncher(JobRepository repo,
                                        @Qualifier("batchTaskExecutor") TaskExecutor exec)
            throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(repo);
        launcher.setTaskExecutor(exec);
        launcher.afterPropertiesSet();
        return launcher;
    }
}
