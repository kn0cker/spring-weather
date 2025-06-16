package com.example.demo.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JobLuncherConfig {

    @Bean
    public CommandLineRunner runJob(JobLauncher jobLauncher, Job simpleJob) {
        return args -> {
            JobParameters parameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // Unique ID
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(simpleJob, parameters);
            System.out.println("Job Status: " + execution.getStatus());
        };
    }
}
